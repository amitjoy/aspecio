package com.amitinside.aspecio.service;

import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_WEAVE;
import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL;
import static com.amitinside.aspecio.api.AspecioConstants._SERVICE_ASPECT_WOVEN;
import static com.amitinside.aspecio.service.ServiceScope.fromString;
import static com.amitinside.aspecio.service.WovenServiceEvent.SERVICE_DEPARTURE;
import static com.amitinside.aspecio.service.WovenServiceEvent.SERVICE_REGISTRATION;
import static com.amitinside.aspecio.service.WovenServiceEvent.ChangeEvent.OPTIONAL_ASPECT_CHANGE;
import static com.amitinside.aspecio.service.WovenServiceEvent.ChangeEvent.REQUIRED_ASPECT_CHANGE;
import static com.amitinside.aspecio.service.WovenServiceEvent.ChangeEvent.SERVICE_PROPERTIES_CHANGE;
import static com.amitinside.aspecio.service.WovenServiceEvent.EventKind.SERVICE_UPDATE;
import static com.amitinside.aspecio.util.AspecioUtil.asInt;
import static com.amitinside.aspecio.util.AspecioUtil.asLong;
import static com.amitinside.aspecio.util.AspecioUtil.asString;
import static com.amitinside.aspecio.util.AspecioUtil.asStringArray;
import static java.util.stream.Collectors.joining;
import static org.osgi.framework.Bundle.START_TRANSIENT;
import static org.osgi.framework.Bundle.STOP_TRANSIENT;
import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.Constants.SERVICE_BUNDLEID;
import static org.osgi.framework.Constants.SERVICE_ID;
import static org.osgi.framework.Constants.SERVICE_PID;
import static org.osgi.framework.Constants.SERVICE_RANKING;
import static org.osgi.framework.Constants.SERVICE_SCOPE;
import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.MODIFIED_ENDMATCH;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.osgi.framework.AllServiceListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.aspecio.service.WovenServiceEvent.ChangeEvent;
import com.github.gfx.util.WeakIdentityHashMap;

import io.primeval.reflex.proxy.bytecode.BridgingClassLoader;
import io.primeval.reflex.proxy.bytecode.Proxy;
import io.primeval.reflex.proxy.bytecode.ProxyBuilder;
import io.primeval.reflex.proxy.bytecode.ProxyClass;
import io.primeval.reflex.proxy.bytecode.ProxyClassLoader;

public final class ServiceWeavingManager implements AllServiceListener {

    private final Logger logger = LoggerFactory.getLogger(ServiceWeavingManager.class);

    private static final String SERVICE_FILTER = MessageFormat.format("(&(|({0}=*)({1}=*))(!({2}=*)))",
            SERVICE_ASPECT_WEAVE, SERVICE_ASPECT_WEAVE_OPTIONAL, _SERVICE_ASPECT_WOVEN);

    private final Map<ServiceReference<?>, WovenService> wovenServiceByServiceRef = new ConcurrentSkipListMap<>();
    private final Map<String, List<WovenService>>        wovenServicesByAspect    = new ConcurrentHashMap<>();
    private final List<WovenServiceListener>             wovenServiceListeners    = new CopyOnWriteArrayList<>();

    // Everything in here is weak, using identity equality, so it nicely cleans up by itself as
    // bundles are cleaned-up, if there are no stale references on our bundles or services of course
    private final Map<BundleRevision, BundleRevisionPath> revisionMap = Collections
            .synchronizedMap(new WeakIdentityHashMap<>());

    private final BundleContext bundleContext;
    private final AtomicBoolean closed = new AtomicBoolean();

    public ServiceWeavingManager(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void open() {
        try {
            bundleContext.addServiceListener(this, SERVICE_FILTER);
            final ServiceReference<?>[] serviceReferences = bundleContext.getAllServiceReferences((String) null,
                    SERVICE_FILTER);
            if (serviceReferences != null) {
                final Set<Bundle> bundlesToRestart = new TreeSet<>();
                Stream.of(serviceReferences).forEach(sr -> bundlesToRestart.add(sr.getBundle()));
                final int bundleRestartCount = bundlesToRestart.size();
                if (bundleRestartCount > 0) {
                    final String bundlesList = bundlesToRestart.stream().map(Bundle::toString).collect(joining(", "));
                    if (bundlesToRestart.size() == 1) {
                        logger.info("Aspecio: active bundle {} requires weaving... restarting it", bundlesList);
                    } else if (bundlesToRestart.size() > 1) {
                        logger.info("Aspecio: active bundles {} require weaving... restarting them", bundlesList);
                    }
                    for (final Bundle b : bundlesToRestart) {
                        try {
                            b.stop(STOP_TRANSIENT);
                        } catch (final BundleException e) {
                            logger.error("Could not stop bundle {}", b, e);
                        }
                    }
                    for (final Bundle b : bundlesToRestart) {
                        try {
                            b.start(START_TRANSIENT);
                        } catch (final BundleException e) {
                            logger.error("Could not start bundle {}", b, e);
                        }
                    }
                }
            }
        } catch (final InvalidSyntaxException e) {
            throw new AssertionError("Could not create filter!", e);
        }
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            bundleContext.removeServiceListener(this);
            wovenServiceByServiceRef.keySet().forEach(bundleContext::ungetService);
            wovenServiceByServiceRef.clear();
        }
    }

    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (closed.get()) {
            return;
        }
        final ServiceReference<?> sr = event.getServiceReference();
        switch (event.getType()) {
            case REGISTERED:
                onServiceRegistration(sr);
                break;
            case MODIFIED:
                onServiceUpdate(sr);
                break;
            case MODIFIED_ENDMATCH:
            case UNREGISTERING:
                onServiceDeparture(sr);
                break;
        }
    }

    private synchronized void onServiceRegistration(final ServiceReference<?> reference) {
        if (wovenServiceByServiceRef.containsKey(reference)) {
            // This might happen if a service arrives between the listener registration and the initial
            // getAllServiceReferences call
            return;
        }
        final long originalServiceId = asLong(reference.getProperty(SERVICE_ID));

        logger.debug("Preparing the weaving service ID {} provided by {}", originalServiceId,
                reference.getBundle().getSymbolicName());

        final List<String> requiredAspectsToWeave = new ArrayList<>(
                Arrays.asList(asStringArray(reference.getProperty(SERVICE_ASPECT_WEAVE))));
        final List<String> optionalAspectsToWeave = new ArrayList<>(
                Arrays.asList(asStringArray(reference.getProperty(SERVICE_ASPECT_WEAVE_OPTIONAL))));
        final List<String> objectClass            = new ArrayList<>(
                Arrays.asList(asStringArray(reference.getProperty(OBJECTCLASS))));
        int                serviceRanking         = asInt(reference.getProperty(SERVICE_RANKING), 0);
        final ServiceScope serviceScope           = fromString(asString(reference.getProperty(SERVICE_SCOPE)));

        // Keep original properties, except for managed ones.
        final Hashtable<String, Object> serviceProperties = new Hashtable<>(); // NOSONAR
        for (final String key : reference.getPropertyKeys()) {
            final Object val = reference.getProperty(key);
            switch (key) {
                case SERVICE_ID:
                case SERVICE_PID:
                case SERVICE_BUNDLEID:
                case SERVICE_RANKING:
                case OBJECTCLASS:
                case SERVICE_SCOPE:
                case SERVICE_ASPECT_WEAVE:
                case SERVICE_ASPECT_WEAVE_OPTIONAL:
                    continue;
                default:
                    serviceProperties.put(key, val);
            }
        }
        serviceRanking++;

        // Check if we can weave it
        final List<Class<?>> interfaces = new ArrayList<>();
        for (final String intf : objectClass) {
            try {
                final Class<?> cls = reference.getBundle().loadClass(intf);
                if (!cls.isInterface()) {
                    // Cannot weave!
                    logger.warn(
                            "Cannot weave service ID {} because it does not implement well-defined service interfaces, such as {}",
                            originalServiceId, cls.getName());
                    bundleContext.ungetService(reference);
                    return;
                }
                interfaces.add(cls);
            } catch (final ClassNotFoundException e) {
                // Should not happen
                logger.error("Could not find class, not weaving service ID {}", originalServiceId, e);
                bundleContext.ungetService(reference);
                return;
            }
        }
        serviceProperties.put(SERVICE_RANKING, serviceRanking);

        final AspecioServiceObject aspecioServiceObject = new AspecioServiceObject(serviceScope, reference,
                originalService -> weave(interfaces, originalService));

        final WovenService wovenService = new WovenService(originalServiceId, requiredAspectsToWeave,
                optionalAspectsToWeave, reference, objectClass, serviceProperties, aspecioServiceObject);
        wovenServiceByServiceRef.put(reference, wovenService);

        final Iterator<String> aspectIt = Stream
                .concat(requiredAspectsToWeave.stream(), optionalAspectsToWeave.stream()).distinct().iterator();
        while (aspectIt.hasNext()) {
            final String             aspect        = aspectIt.next();
            final List<WovenService> wovenServices = wovenServicesByAspect.computeIfAbsent(aspect,
                    k -> new ArrayList<>());
            wovenServices.add(wovenService);
        }
        fireEvent(SERVICE_REGISTRATION, wovenService);
    }

    private synchronized void onServiceUpdate(final ServiceReference<?> reference) {
        final WovenService wovenService = wovenServiceByServiceRef.get(reference);
        if (wovenService == null) {
            return;
        }
        final List<String> requiredAspectsToWeave = new ArrayList<>(
                Arrays.asList(asStringArray(reference.getProperty(SERVICE_ASPECT_WEAVE))));
        final List<String> optionalAspectsToWeave = new ArrayList<>(
                Arrays.asList(asStringArray(reference.getProperty(SERVICE_ASPECT_WEAVE_OPTIONAL))));
        int                serviceRanking         = asInt(reference.getProperty(SERVICE_RANKING), 0);

        // Keep original properties, except for managed ones.
        final Hashtable<String, Object> serviceProperties = new Hashtable<>(); // NOSONAR
        for (final String key : reference.getPropertyKeys()) {
            final Object val = reference.getProperty(key);
            switch (key) {
                case SERVICE_ID:
                case SERVICE_PID:
                case SERVICE_BUNDLEID:
                case SERVICE_RANKING:
                case OBJECTCLASS:
                case SERVICE_SCOPE:
                case SERVICE_ASPECT_WEAVE:
                case SERVICE_ASPECT_WEAVE_OPTIONAL:
                    continue;
                default:
                    serviceProperties.put(key, val);
            }
        }
        serviceRanking++;
        serviceProperties.put(SERVICE_RANKING, serviceRanking);

        final boolean requiredAspectsChanged   = !Objects.equals(requiredAspectsToWeave, wovenService.requiredAspects);
        final boolean optionalAspectsChanged   = !Objects.equals(optionalAspectsToWeave, wovenService.optionalAspects);
        final boolean servicePropertiesChanged = !Objects.equals(serviceProperties, wovenService.serviceProperties);

        final WovenService updatedWovenService = wovenService.update(requiredAspectsToWeave, optionalAspectsToWeave,
                serviceProperties);

        final EnumSet<ChangeEvent> events = EnumSet.noneOf(ChangeEvent.class);
        if (requiredAspectsChanged) {
            events.add(REQUIRED_ASPECT_CHANGE);
        }
        if (optionalAspectsChanged) {
            events.add(OPTIONAL_ASPECT_CHANGE);
        }
        if (servicePropertiesChanged) {
            events.add(SERVICE_PROPERTIES_CHANGE);
        }
        if (!events.isEmpty()) {
            fireEvent(new WovenServiceEvent(SERVICE_UPDATE, events), updatedWovenService);
        }
    }

    private synchronized void onServiceDeparture(final ServiceReference<?> reference) {
        final WovenService wovenService = wovenServiceByServiceRef.get(reference);
        if (wovenService == null) {
            return;
        }
        fireEvent(SERVICE_DEPARTURE, wovenService);
    }

    private Proxy weave(final List<Class<?>> interfaces, final Object delegateToWeave) {
        final ProxyClassLoader dynamicClassLoader = getDynamicClassLoader(delegateToWeave);

        final ProxyClass<? extends Object> proxyClass = ProxyBuilder.build(dynamicClassLoader,
                delegateToWeave.getClass(), interfaces.toArray(new Class<?>[0]));
        return proxyClass.newInstance(delegateToWeave);
    }

    private ProxyClassLoader getDynamicClassLoader(final Object delegateToWeave) {
        return getDynamicClassLoader(delegateToWeave.getClass());
    }

    private ProxyClassLoader getDynamicClassLoader(final Class<?> clazz) {
        // Find all bundles required to instantiate the class and bridge their
        // classloaders in case the abstract class or interface lives in non-imported packages
        Class<?>                                currClazz     = clazz;
        final List<BundleRevision>              bundleRevs    = new ArrayList<>();
        Map<BundleRevision, BundleRevisionPath> revisions     = revisionMap;
        BundleRevisionPath                      bundleRevPath = null;
        do {
            final BundleRevision bundleRev = FrameworkUtil.getBundle(currClazz).adapt(BundleRevision.class);
            if (!bundleRevs.contains(bundleRev)) {
                bundleRevs.add(bundleRev);
                bundleRevPath = revisions.computeIfAbsent(bundleRev, k -> new BundleRevisionPath());
                revisions     = bundleRevPath
                        .computeSubMapIfAbsent(() -> Collections.synchronizedMap(new WeakIdentityHashMap<>()));
            }
            currClazz = currClazz.getSuperclass();
        } while (currClazz != null && currClazz != Object.class);

        return bundleRevPath.computeClassLoaderIfAbsent(() -> {
            // the bundles set is now prioritized ...
            final ClassLoader[] classLoaders = bundleRevs.stream().map(b -> b.getWiring().getClassLoader())
                    .toArray(ClassLoader[]::new);
            return new ProxyClassLoader(new BridgingClassLoader(classLoaders));
        });
    }

    private void fireEvent(final WovenServiceEvent event, final WovenService wovenService) {
        wovenServiceListeners.forEach(l -> l.onWovenServiceEvent(event, wovenService));
    }

    public void addListener(final WovenServiceListener wovenServiceListener) {
        wovenServiceListeners.add(wovenServiceListener);
    }

    public void removeListener(final WovenServiceListener wovenServiceListener) {
        wovenServiceListeners.remove(wovenServiceListener);
    }

    public List<WovenService> getWovenServicesForAspect(final String aspectName) {
        return wovenServicesByAspect.get(aspectName);
    }

}
