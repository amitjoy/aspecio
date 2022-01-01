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

import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_WOVEN;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.aspecio.util.Exceptions;

// Owned by AspecioServiceController (i.e, sync is done there)
public final class ManagedWovenService {

    private final Logger logger = LoggerFactory.getLogger(ManagedWovenService.class);

    // can be null if unsatisfied
    WovenService wovenService;
    AspectInterceptorContext aspectContext;
    ServiceRegistration<?> registration;

    public Dictionary<String, Object> getProperties() {
        final Map<String, Object> props = new HashMap<>();
        props.putAll(wovenService.serviceProperties);
        props.put(SERVICE_ASPECT_WOVEN, aspectContext.satisfiedAspects.toArray(new String[0]));

        return new Hashtable<>(props);
    }

    public void register() {
        logger.debug("Registering aspect proxy for service {} with aspects {}", wovenService.originalServiceId,
                aspectContext.satisfiedAspects);

        registration = wovenService.originalReference.getBundle().getBundleContext().registerService(
                wovenService.objectClass.toArray(new String[0]),
                wovenService.aspecioServiceObject.getServiceObjectToRegister(), getProperties());
    }

    public void unregister() {
        if (registration == null) {
            return;
        }
        logger.debug("Deregistering aspect proxy for service ID {}", wovenService.originalServiceId);
        try {
            registration.unregister();
        } catch (final IllegalStateException e) {
            throw Exceptions.duck(e);
        } finally {
            registration = null;
        }

    }

}
