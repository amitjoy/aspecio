/*******************************************************************************
 * Copyright 2022 Amit Kumar Mondal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.amitinside.aspecio.service;

import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT;
import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_EXTRAPROPERTIES;
import static com.amitinside.aspecio.service.AspectInterceptorListener.EventKind.NEW_MATCH;
import static com.amitinside.aspecio.service.AspectInterceptorListener.EventKind.NO_MATCH;
import static com.amitinside.aspecio.util.AspecioUtil.asInt;
import static com.amitinside.aspecio.util.AspecioUtil.asLong;
import static com.amitinside.aspecio.util.AspecioUtil.asSet;
import static com.amitinside.aspecio.util.AspecioUtil.asString;
import static com.amitinside.aspecio.util.AspecioUtil.firstOrNull;
import static java.util.Comparator.comparing;
import static org.osgi.framework.Constants.SERVICE_BUNDLEID;
import static org.osgi.framework.Constants.SERVICE_ID;
import static org.osgi.framework.Constants.SERVICE_RANKING;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.aspecio.api.AspectDTO;
import com.amitinside.aspecio.api.InterceptorDTO;
import com.amitinside.aspecio.service.AspectInterceptorListener.EventKind;

import io.primeval.reflex.proxy.Interceptor;
import io.primeval.reflex.proxy.Interceptors;

public final class AspectInterceptorManager implements ServiceTrackerCustomizer<Object, Object> {

    private static final String SERVICE_FILTER = "(" + SERVICE_ASPECT + "=*)";

    private final Logger logger = LoggerFactory.getLogger(AspectInterceptorManager.class);
    private final BundleContext bundleContext;

    private final SortedMap<ServiceReference<?>, AspectInterceptor> aspectServiceByServiceRef = new ConcurrentSkipListMap<>();
    private final Map<String, SortedSet<AspectInterceptor>> aspectServicesByAspectName = new ConcurrentHashMap<>();
    private final List<AspectInterceptorListener> aspectInterceptorListeners = new CopyOnWriteArrayList<>();

    private ServiceTracker<Object, Object> tracker;

    public AspectInterceptorManager(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void open() throws InvalidSyntaxException {
        final Filter filter = bundleContext.createFilter(SERVICE_FILTER);
        tracker = new ServiceTracker<>(bundleContext, filter, this);
        tracker.open();
    }

    public void close() {
        tracker.close();
        synchronized (this) {
            aspectServiceByServiceRef.keySet().forEach(bundleContext::ungetService);
            aspectServiceByServiceRef.clear();
        }
    }

    @Override
    public Object addingService(final ServiceReference<Object> reference) {
        final Object service = bundleContext.getService(reference);
        onServiceRegistration(reference, service);
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference, final Object service) {
        onServiceUpdate(reference);
    }

    @Override
    public void removedService(final ServiceReference<Object> reference, final Object service) {
        onServiceDeparture(reference);
    }

    public synchronized void onServiceRegistration(final ServiceReference<?> reference, final Object service) {
        final String aspect = asString(reference.getProperty(SERVICE_ASPECT));
        final Set<String> extraProperties = asSet(reference.getProperty(SERVICE_ASPECT_EXTRAPROPERTIES));
        final int serviceRanking = asInt(reference.getProperty(SERVICE_RANKING), 0);

        if (!(service instanceof Interceptor)) {
            // Don't track aspects that don't implement Interceptor.
            bundleContext.ungetService(reference);
            return;
        }
        logger.debug("Added aspect: {} (extraProps: {})", aspect, extraProperties);
        final AspectInterceptor aspectService = new AspectInterceptor(aspect, (Interceptor) service, reference,
                serviceRanking, extraProperties);
        aspectServiceByServiceRef.put(reference, aspectService);

        // Deal with aspect map.
        final SortedSet<AspectInterceptor> as = aspectServicesByAspectName.computeIfAbsent(aspect,
                k -> new TreeSet<>());
        final AspectInterceptor firstBefore = firstOrNull(as);
        // The trick here is that we use a SortedSet with the right compareTo method on aspectService.
        as.add(aspectService);

        final AspectInterceptor firstAfter = firstOrNull(as);
        if (firstAfter != firstBefore) {
            // still in lock, should we?
            fireEvent(NEW_MATCH, aspect, firstAfter);
        }
    }

    public synchronized void onServiceUpdate(final ServiceReference<?> reference) {
        final AspectInterceptor aspectService = aspectServiceByServiceRef.get(reference);
        if (aspectService == null) {
            return;
        }
        final String newAspect = asString(reference.getProperty(SERVICE_ASPECT));
        final Set<String> extraProperties = asSet(reference.getProperty(SERVICE_ASPECT_EXTRAPROPERTIES));
        final int serviceRanking = asInt(reference.getProperty(SERVICE_RANKING), 0);

        final boolean rankingChanged = aspectService.serviceRanking != serviceRanking;
        final boolean aspectChanged = !Objects.equals(aspectService.aspect, newAspect);
        final boolean extraPropsChanged = !Objects.equals(aspectService.extraProperties, extraProperties);

        if (rankingChanged || aspectChanged || extraPropsChanged) {
            if (!aspectChanged) {
                logger.debug("Updating aspect: {} (extraProps: {})", newAspect, extraProperties);
            } else {
                logger.debug("Updating aspect: {} -> {} (extraProps: {})", aspectService.aspect, newAspect,
                        extraProperties);
            }
            final AspectInterceptor updatedService = new AspectInterceptor(newAspect, aspectService.interceptor,
                    reference, serviceRanking, extraProperties);
            aspectServiceByServiceRef.put(reference, updatedService);

            final Iterator<String> aspectsToProcess = Stream.of(aspectService.aspect, newAspect).distinct().iterator();

            while (aspectsToProcess.hasNext()) {
                final String aspect = aspectsToProcess.next();
                final boolean toPublish = newAspect.equals(aspect);
                final SortedSet<AspectInterceptor> as = aspectServicesByAspectName.computeIfAbsent(aspect,
                        k -> new TreeSet<>());
                final AspectInterceptor firstBefore = firstOrNull(as);

                if (toPublish) {
                    if (rankingChanged) {
                        // special case where we must force the re-ordering
                        // by cleanly removing from the set first
                        as.remove(aspectService);
                    }
                    // The trick here is that we use a SortedSet
                    // with the right compareTo method on aspectService.
                    // It will replace the pre-existing service that has
                    // a different entity, but compareTo() == 0.
                    as.add(updatedService);
                } else {
                    // here it is the *old* service we remove.
                    as.remove(aspectService);
                    // clean-up
                    if (as.isEmpty()) {
                        aspectServicesByAspectName.remove(aspect);
                    }
                }
                final AspectInterceptor firstAfter = firstOrNull(as);
                if (firstAfter != firstBefore) {
                    // still in lock, should we?
                    fireEvent(firstAfter != null ? NEW_MATCH : NO_MATCH, aspect, firstAfter);
                }
            }
        }

    }

    public synchronized void onServiceDeparture(final ServiceReference<?> reference) {
        final AspectInterceptor aspectService = aspectServiceByServiceRef.get(reference);
        if (aspectService == null) {
            return;
        }
        final String aspect = aspectService.aspect;
        logger.debug("Removed aspect: {} (extraProps: {})", aspect, aspectService.extraProperties);
        aspectServiceByServiceRef.remove(reference);

        final SortedSet<AspectInterceptor> as = aspectServicesByAspectName.get(aspect);
        final AspectInterceptor firstBefore = firstOrNull(as);

        if (as != null) {
            as.remove(aspectService);
            if (as.isEmpty()) {
                aspectServicesByAspectName.remove(aspect);
            }
        }
        final AspectInterceptor firstAfter = firstOrNull(as);
        if (firstAfter != firstBefore) {
            // still in lock, should we?
            fireEvent(firstAfter != null ? NEW_MATCH : NO_MATCH, aspect, firstAfter);
        }
    }

    public synchronized AspectInterceptorContext getContext(final List<String> requiredAspects,
            final List<String> optionalAspects) {
        final Set<AspectInterceptor> interceptors = new TreeSet<>(
                comparing((final AspectInterceptor a) -> a.aspect).thenComparingInt(a -> a.serviceRanking));
        final Set<String> satisfiedRequiredAspects = new LinkedHashSet<>();
        final Set<String> unsatisfiedRequiredAspects = new LinkedHashSet<>();
        final Set<String> satisfiedOptionalAspects = new LinkedHashSet<>();
        final Set<String> unsatisfiedOptionalAspects = new LinkedHashSet<>();
        final Set<String> extraProperties = new LinkedHashSet<>();

        for (final String aspect : requiredAspects) {
            final AspectInterceptor aspectInterceptor = getAspectInterceptor(aspect);
            if (aspectInterceptor != null) {
                satisfiedRequiredAspects.add(aspect);
                extraProperties.addAll(aspectInterceptor.extraProperties);
                interceptors.add(aspectInterceptor);
            } else {
                unsatisfiedRequiredAspects.add(aspect);
            }
        }
        for (final String aspect : optionalAspects) {
            final AspectInterceptor aspectInterceptor = getAspectInterceptor(aspect);
            if (aspectInterceptor != null) {
                satisfiedOptionalAspects.add(aspect);
                extraProperties.addAll(aspectInterceptor.extraProperties);
                interceptors.add(aspectInterceptor);
            } else {
                unsatisfiedOptionalAspects.add(aspect);
            }
        }
        final Set<String> satisfiedAspects = new LinkedHashSet<>();
        satisfiedAspects.addAll(satisfiedRequiredAspects);
        satisfiedAspects.addAll(satisfiedOptionalAspects);
        final Interceptor interceptor = Interceptors.stack(interceptors.stream().map(ai -> ai.interceptor).iterator());

        return new AspectInterceptorContext(interceptor, satisfiedAspects, satisfiedRequiredAspects,
                unsatisfiedRequiredAspects, satisfiedOptionalAspects, unsatisfiedOptionalAspects, extraProperties);
    }

    private void fireEvent(final EventKind eventKind, final String aspectName,
            final AspectInterceptor aspectInterceptor) {
        aspectInterceptorListeners.forEach(l -> l.onAspectChange(eventKind, aspectName, aspectInterceptor));
    }

    public void addListener(final AspectInterceptorListener aspectInterceptorListener) {
        aspectInterceptorListeners.add(aspectInterceptorListener);
    }

    public void removeListener(final AspectInterceptorListener aspectInterceptorListener) {
        aspectInterceptorListeners.remove(aspectInterceptorListener);
    }

    private AspectInterceptor getAspectInterceptor(final String aspectName) {
        return firstOrNull(aspectServicesByAspectName.get(aspectName));
    }

    public synchronized Set<String> getRegisteredAspects() {
        return asSet(aspectServicesByAspectName.keySet());
    }

    public synchronized Optional<AspectDTO> getAspectDescription(final String aspectName) {
        final SortedSet<AspectInterceptor> ais = aspectServicesByAspectName.get(aspectName);
        if (ais == null || ais.isEmpty()) {
            return Optional.empty();
        }
        final Iterator<AspectInterceptor> iterator = ais.iterator();

        AspectInterceptor interceptor = iterator.next();
        final InterceptorDTO id = makeInterceptorDTO(interceptor);
        final List<InterceptorDTO> backupIds = new ArrayList<>(ais.size() - 1);

        while (iterator.hasNext()) {
            interceptor = iterator.next();
            final InterceptorDTO backupId = makeInterceptorDTO(interceptor);
            backupIds.add(backupId);
        }
        final AspectDTO ad = new AspectDTO();
        ad.aspectName = aspectName;
        ad.interceptor = id;
        ad.backupInterceptors = backupIds;

        return Optional.of(ad);
    }

    private InterceptorDTO makeInterceptorDTO(final AspectInterceptor ai) {
        final long serviceId = asLong(ai.serviceRef.getProperty(SERVICE_ID));
        final long bundleId = asLong(ai.serviceRef.getProperty(SERVICE_BUNDLEID));

        final InterceptorDTO dto = new InterceptorDTO();
        dto.serviceId = serviceId;
        dto.bundleId = bundleId;
        dto.serviceRanking = ai.serviceRanking;
        dto.interceptorClass = ai.interceptor.getClass();
        dto.extraProperties = asSet(ai.extraProperties);

        return dto;
    }

}
