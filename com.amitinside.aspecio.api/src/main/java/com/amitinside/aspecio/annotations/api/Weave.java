/*******************************************************************************
 * Copyright 2021-2024 Amit Kumar Mondal
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
package com.amitinside.aspecio.annotations.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.osgi.service.component.annotations.ComponentPropertyType;

import com.amitinside.aspecio.api.AspecioConstants;

/**
 * Component Property Type for Aspect Weaving.
 * <p>
 * This annotation can be used on a {@link org.osgi.service.component.annotations.Component} 
 * to request that aspects be woven into the component. It is used by tools to generate 
 * service properties in the component XML.
 * <p>
 * The annotation is not retained at runtime.
 * 
 * <h2>Properties:</h2>
 * <ul>
 *   <li>{@link AspecioConstants#SERVICE_ASPECT_WEAVE}</li>
 *   <li>{@link AspecioConstants#SERVICE_ASPECT_WEAVE_OPTIONAL}</li>
 * </ul>
 * 
 * @see "Component Property Types"
 */
@Target(TYPE)
@Retention(CLASS)
@ComponentPropertyType
public @interface Weave {

    String PREFIX_ = "service.aspect.weave.";

    /**
     * Specifies the required aspects to weave. The woven service will only be published 
     * if all required aspects are present. The service will be unregistered if any 
     * required aspect is deregistered.
     *
     * @return an array of required aspect classes.
     */
    Class<?>[] required() default {};

    /**
     * Specifies the optional aspects to weave. The woven service will be published 
     * even if the optional aspects are absent. When an optional aspect is registered, 
     * it allows that aspect to intercept the service methods, even if the service was 
     * previously published without that aspect.
     *
     * @return an array of optional aspect classes.
     */
    Class<?>[] optional() default {};

}