/*******************************************************************************
 * Copyright 2022-2025 Amit Kumar Mondal
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

import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_WOVEN;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.amitinside.aspecio.util.Exceptions;

/**
 * Manages the registration and unregistration of a woven service.
 * <p>
 * This class is controlled by AspecioServiceController to ensure thread safety.
 */
public final class ManagedWovenService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public WovenService wovenService;
    public ServiceRegistration<?> registration;
    public AspectInterceptorContext aspectContext;

    /**
     * Creates a dictionary of service properties with the current aspect weaving state.
     *
     * @return a dictionary of service properties.
     */
    public Dictionary<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>(wovenService.serviceProperties);
        props.put(SERVICE_ASPECT_WOVEN, aspectContext.getSatisfiedAspects().toArray(new String[0]));

        return new Hashtable<>(props);
    }

    /**
     * Registers the woven service with the OSGi framework.
     */
    public void register() {
        if (registration != null) {
            logger.warn("Service is already registered: {}", wovenService.originalServiceId);
            return;
        }
        
        logger.debug("Registering aspect proxy for service {} with aspects {}", 
                wovenService.originalServiceId, aspectContext.getSatisfiedAspects());

        try {
            registration = wovenService.originalReference.getBundle().getBundleContext().registerService(
                    wovenService.objectClass.toArray(new String[0]),
                    wovenService.aspecioServiceObject.getServiceObjectToRegister(), 
                    getProperties());
        } catch (Exception e) {
            logger.error("Failed to register service {}: {}", wovenService.originalServiceId, e.getMessage());
            throw Exceptions.duck(e);
        }
    }

    /**
     * Unregisters the woven service from the OSGi framework.
     */
    public void unregister() {
        if (registration == null) {
            logger.warn("Service is not registered: {}", wovenService.originalServiceId);
            return;
        }

        logger.debug("Deregistering aspect proxy for service ID {}", wovenService.originalServiceId);
        try {
            registration.unregister();
        } catch (IllegalStateException e) {
            logger.error("Service already unregistered or in invalid state: {}", e.getMessage());
            throw Exceptions.duck(e);
        } finally {
            registration = null;
        }
    }
}