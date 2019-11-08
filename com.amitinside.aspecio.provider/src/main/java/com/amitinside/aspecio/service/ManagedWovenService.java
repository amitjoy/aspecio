package com.amitinside.aspecio.service;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.Logger;
import com.amitinside.aspecio.api.AspecioConstants;
import com.amitinside.aspecio.logging.AspecioLogger;


// Owned by AspecioServiceController (i.e, sync is done there)
public final class ManagedWovenService {

  private final Logger logger = AspecioLogger.getLogger(ManagedWovenService.class);

  // can be null if unsatisfied
  public WovenService             wovenService;
  public AspectInterceptorContext aspectContext;
  public ServiceRegistration<?>   registration;

  public Dictionary<String, Object> getProperties() {
    final Hashtable<String, Object> props = new Hashtable<>();
    props.putAll(wovenService.serviceProperties);
    props.put(AspecioConstants._SERVICE_ASPECT_WOVEN,
        aspectContext.satisfiedAspects.toArray(new String[0]));

    return props;
  }

  public void register() {
    logger.debug("Registering aspect proxy for service {} with aspects {}",
        wovenService.originalServiceId, aspectContext.satisfiedAspects);

    registration = wovenService.originalReference.getBundle().getBundleContext().registerService(
        wovenService.objectClass.toArray(new String[0]),
        wovenService.aspecioServiceObject.getServiceObjectToRegister(), getProperties());
  }

  public void unregister() {
    if (registration == null) {
      return;
    }
    logger.debug("Unregistering aspect proxy for serviceId {}", wovenService.originalServiceId);
    try {
      registration.unregister();
    } catch (final IllegalStateException ise) {
      // ignore, can happen if the remote bundle is gone before on some fwks
    } finally {
      registration = null;
    }

  }

}
