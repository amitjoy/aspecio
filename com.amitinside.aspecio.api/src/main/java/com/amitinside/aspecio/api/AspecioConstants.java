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
package com.amitinside.aspecio.api;

/**
 * Public constants in Aspecio.
 */
public final class AspecioConstants {

    private AspecioConstants() {
        throw new IllegalAccessError("Cannot be instantiated");
    }

    /**
     * Whether or not Aspecio should filter away services that ask for weaving, effectively hiding
     * them from all bundles except the system bundle and Aspecio itself. Set that property to
     * {@literal "false"} in the OSGi framework property or as Java system properties to change
     * Aspecio's behavior.
     */
    public static final String ASPECIO_FILTER_SERVICES = "aspecio.filter.services";

    /**
     * The property a service should register to require Aspecio to weave one or several aspects. The
     * property should be of type String[].
     */
    public static final String SERVICE_ASPECT_WEAVE = "service.aspect.weave.required";

    /**
     * The property a service should register to ask Aspecio to optionally weave one or several
     * aspects. The property should be of type String[].
     */
    public static final String SERVICE_ASPECT_WEAVE_OPTIONAL = "service.aspect.weave.optional";

    /**
     * The property a service should register to declare an Aspect. The service object registered
     * should be assignable to {@code Interceptor} to be recognized as a proper Aspect.
     */
    public static final String SERVICE_ASPECT = "service.aspect.name";

    /**
     * The property a service should register, on an Aspect, to ask Aspecio to declare a Boolean
     * property in woven services set to {@link Boolean#TRUE}.
     */
    public static final String SERVICE_ASPECT_EXTRAPROPERTIES = "service.aspect.extraProperties";

    /**
     * The hidden property containing the array of Aspects effectively woven at any given time, for a
     * woven service.
     */
    public static final String SERVICE_ASPECT_WOVEN = ".service.aspect.woven";

}
