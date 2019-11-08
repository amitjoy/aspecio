package com.amitinside.aspecio.service.provider;

import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_WEAVE;
import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL;
import static org.osgi.namespace.service.ServiceNamespace.SERVICE_NAMESPACE;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.osgi.annotation.bundle.Capability;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.framework.hooks.service.ListenerHook.ListenerInfo;
import org.osgi.service.log.Logger;
import com.amitinside.aspecio.api.Aspecio;
import com.amitinside.aspecio.api.AspectDTO;
import com.amitinside.aspecio.api.InterceptedServiceDTO;
import com.amitinside.aspecio.logging.provider.AspecioLogger;

@Capability(namespace = SERVICE_NAMESPACE,
    attribute = "objectClass:List<String>=com.amitinside.aspecio.api.Aspecio")
public final class AspecioProvider implements Aspecio, FindHook, EventListenerHook {

  private final Logger logger = AspecioLogger.getLogger(AspecioProvider.class);

  private final long                     bundleId;
  private final ServiceWeavingManager    serviceWeavingManager;
  private final AspectInterceptorManager aspectInterceptorManager;
  private final AspecioServiceController aspecioServiceController;

  public AspecioProvider(final BundleContext bundleContext) {
    bundleId = bundleContext.getBundle().getBundleId();

    aspectInterceptorManager = new AspectInterceptorManager(bundleContext);
    serviceWeavingManager    = new ServiceWeavingManager(bundleContext);
    aspecioServiceController =
        new AspecioServiceController(aspectInterceptorManager, serviceWeavingManager);
  }

  public void activate() {
    logger.info("Activating Aspecio");
    aspecioServiceController.open();
    logger.info("Aspecio activated");
  }

  public void deactivate() {
    aspecioServiceController.close();
    logger.info("Aspecio deactivated");
  }

  @Override
  public void event(final ServiceEvent event,
      final Map<BundleContext, Collection<ListenerInfo>> listeners) {
    // Is it an event we want to filter out?
    if (event.getServiceReference().getProperty(SERVICE_ASPECT_WEAVE) == null
        && event.getServiceReference().getProperty(SERVICE_ASPECT_WEAVE_OPTIONAL) == null) {
      return;
    }
    final Iterator<BundleContext> iterator = listeners.keySet().iterator();
    while (iterator.hasNext()) {
      final BundleContext consumingBc       = iterator.next();
      final long          consumingBundleId = consumingBc.getBundle().getBundleId();

      if (consumingBundleId == bundleId || consumingBundleId == 0) {
        continue; // allow self and system bundle
      }
      iterator.remove();
    }
  }

  @Override
  public void find(final BundleContext context, final String name, final String filter,
      final boolean allServices, final Collection<ServiceReference<?>> references) {
    final long consumingBundleId = context.getBundle().getBundleId();
    if (consumingBundleId == bundleId || consumingBundleId == 0) {
      return;
    }
    final Iterator<ServiceReference<?>> iterator = references.iterator();
    while (iterator.hasNext()) {
      final ServiceReference<?> reference = iterator.next();
      if (reference.getProperty(SERVICE_ASPECT_WEAVE) == null
          && reference.getProperty(SERVICE_ASPECT_WEAVE_OPTIONAL) == null) {
        continue;
      }
      iterator.remove();
    }
  }

  @Override
  public Set<String> getRegisteredAspects() {
    return aspectInterceptorManager.getRegisteredAspects();
  }

  @Override
  public Optional<AspectDTO> getAspectDescription(final String aspectName) {
    return aspectInterceptorManager.getAspectDescription(aspectName);
  }

  @Override
  public List<InterceptedServiceDTO> getInterceptedServices() {
    return aspecioServiceController.getInterceptedServices();
  }

}
