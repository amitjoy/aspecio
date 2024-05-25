/*******************************************************************************
 * © 2021-2024 Amit Kumar Mondal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 ******************************************************************************/
package com.amitinside.aspecio.api;

/**
 * A class containing public constants used in Aspecio.
 */
public final class AspecioConstants {

    private AspecioConstants() {
        throw new IllegalAccessError("This class cannot be instantiated.");
    }

    /**
     * Property indicating whether Aspecio should filter out services that request 
     * weaving, effectively hiding them from all bundles except the system bundle 
     * and Aspecio itself. Set this property to {@literal "false"} in the OSGi 
     * framework or as Java system properties to modify Aspecio's behavior.
     */
    public static final String ASPECIO_FILTER_SERVICES = "aspecio.filter.services";

    /**
     * Property a service should register to require Aspecio to weave one or 
     * several aspects. This property should be of type String[].
     */
    public static final String SERVICE_ASPECT_WEAVE = "service.aspect.weave.required";

    /**
     * Property a service should register to request Aspecio to optionally weave 
     * one or several aspects. This property should be of type String[].
     */
    public static final String SERVICE_ASPECT_WEAVE_OPTIONAL = "service.aspect.weave.optional";

    /**
     * Property a service should register to declare an aspect. The registered 
     * service object should be assignable to {@code Interceptor} to be recognized 
     * as a valid aspect.
     */
    public static final String SERVICE_ASPECT = "service.aspect.name";

    /**
     * Property a service should register on an aspect to request Aspecio to 
     * declare a Boolean property in woven services set to {@link Boolean#TRUE}.
     */
    public static final String SERVICE_ASPECT_EXTRAPROPERTIES = "service.aspect.extraProperties";

    /**
     * Hidden property containing the array of aspects effectively woven at any 
     * given time for a woven service.
     */
    public static final String SERVICE_ASPECT_WOVEN = ".service.aspect.woven";

}
