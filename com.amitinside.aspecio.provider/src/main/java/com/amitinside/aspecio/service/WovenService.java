/*******************************************************************************
 * Copyright 2022-2024 Amit Kumar Mondal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.amitinside.aspecio.service;

import java.util.List;
import java.util.Map;

import org.osgi.framework.ServiceReference;

/**
 * Represents a service that has been woven with required and optional aspects.
 * This class encapsulates information about the original service and the 
 * aspects applied to it.
 */
public final class WovenService {

    /** ID of the original service. */
    public final long originalServiceId;
    
    /** List of required aspects to be woven into the service. */
    public final List<String> requiredAspects;
    
    /** List of optional aspects to be woven into the service. */
    public final List<String> optionalAspects;
    
    /** Reference to the original OSGi service. */
    public final ServiceReference<?> originalReference;
    
    /** List of object classes that the original service implements. */
    public final List<String> objectClass;
    
    /** Properties associated with the service. */
    public final Map<String, Object> serviceProperties;
    
    /** The Aspecio service object associated with this woven service. */
    public final AspecioServiceObject aspecioServiceObject;

    /**
     * Constructs a new {@code WovenService} instance.
     *
     * @param originalServiceId      The ID of the original service.
     * @param requiredAspectsToWeave List of required aspects to be woven.
     * @param optionalAspectsToWeave List of optional aspects to be woven.
     * @param originalReference      Reference to the original OSGi service.
     * @param objectClass            List of object classes the original service implements.
     * @param serviceProperties      Properties associated with the service.
     * @param aspecioServiceObject   The Aspecio service object associated with this service.
     */
    public WovenService(final long originalServiceId, final List<String> requiredAspectsToWeave,
                        final List<String> optionalAspectsToWeave, final ServiceReference<?> originalReference,
                        final List<String> objectClass, final Map<String, Object> serviceProperties,
                        final AspecioServiceObject aspecioServiceObject) {
        this.requiredAspects = requiredAspectsToWeave;
        this.optionalAspects = optionalAspectsToWeave;
        this.originalReference = originalReference;
        this.originalServiceId = originalServiceId;
        this.objectClass = objectClass;
        this.serviceProperties = serviceProperties;
        this.aspecioServiceObject = aspecioServiceObject;
    }

    /**
     * Creates a new {@code WovenService} instance with updated aspects and properties.
     *
     * @param requiredAspects  Updated list of required aspects.
     * @param optionalAspects  Updated list of optional aspects.
     * @param serviceProperties Updated service properties.
     * @return A new {@code WovenService} instance with the updated details.
     */
    public WovenService update(final List<String> requiredAspects, final List<String> optionalAspects,
                               final Map<String, Object> serviceProperties) {
        return new WovenService(originalServiceId, requiredAspects, optionalAspects, originalReference, objectClass,
                                serviceProperties, aspecioServiceObject);
    }
}