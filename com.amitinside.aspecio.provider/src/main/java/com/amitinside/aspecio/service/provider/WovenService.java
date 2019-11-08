package com.amitinside.aspecio.service.provider;

import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.ServiceReference;

public final class WovenService {

  public final long                      originalServiceId;
  public final List<String>              requiredAspects;
  public final List<String>              optionalAspects;
  public final ServiceReference<?>       originalReference;
  public final List<String>              objectClass;
  public final Hashtable<String, Object> serviceProperties;
  public final AspecioServiceObject      aspecioServiceObject;

  public WovenService(final long originalServiceId, final List<String> requiredAspectsToWeave,
      final List<String> optionalAspectsToWeave, final ServiceReference<?> originalReference,
      final List<String> objectClass, final Hashtable<String, Object> serviceProperties,
      final AspecioServiceObject aspecioServiceObject) {
    requiredAspects           = requiredAspectsToWeave;
    optionalAspects           = optionalAspectsToWeave;
    this.originalReference    = originalReference;
    this.originalServiceId    = originalServiceId;
    this.objectClass          = objectClass;
    this.serviceProperties    = serviceProperties;
    this.aspecioServiceObject = aspecioServiceObject;
  }

  public WovenService update(final List<String> requiredAspects, final List<String> optionalAspects,
      final Hashtable<String, Object> serviceProperties) {
    return new WovenService(originalServiceId, requiredAspects, optionalAspects, originalReference,
        objectClass, serviceProperties, aspecioServiceObject);
  }
}
