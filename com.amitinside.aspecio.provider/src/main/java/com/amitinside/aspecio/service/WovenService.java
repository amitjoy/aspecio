/*******************************************************************************
 * Copyright 2021 Amit Kumar Mondal
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

import java.util.List;
import java.util.Map;

import org.osgi.framework.ServiceReference;

public final class WovenService {

    public final long originalServiceId;
    public final List<String> requiredAspects;
    public final List<String> optionalAspects;
    public final ServiceReference<?> originalReference;
    public final List<String> objectClass;
    public final Map<String, Object> serviceProperties;
    public final AspecioServiceObject aspecioServiceObject;

    public WovenService(final long originalServiceId, final List<String> requiredAspectsToWeave,
            final List<String> optionalAspectsToWeave, final ServiceReference<?> originalReference,
            final List<String> objectClass, final Map<String, Object> serviceProperties,
            final AspecioServiceObject aspecioServiceObject) {
        requiredAspects = requiredAspectsToWeave;
        optionalAspects = optionalAspectsToWeave;
        this.originalReference = originalReference;
        this.originalServiceId = originalServiceId;
        this.objectClass = objectClass;
        this.serviceProperties = serviceProperties;
        this.aspecioServiceObject = aspecioServiceObject;
    }

    public WovenService update(final List<String> requiredAspects, final List<String> optionalAspects,
            final Map<String, Object> serviceProperties) {
        return new WovenService(originalServiceId, requiredAspects, optionalAspects, originalReference, objectClass,
                serviceProperties, aspecioServiceObject);
    }
}
