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
package com.amitinside.aspecio.annotations.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.osgi.service.component.annotations.ComponentPropertyType;

import com.amitinside.aspecio.api.AspecioConstants;

/**
 * Component Property Type for Aspect.
 *
 * <ul>
 * <li>{@link AspecioConstants#SERVICE_ASPECT}</li>
 * <li>{@link AspecioConstants#SERVICE_ASPECT_EXTRAPROPERTIES}</li>
 * </ul>
 *
 * <p>
 * This annotation can be used on a {@link Component} to define aspects. The component class must be
 * assignable to {@code io.primeval.reflex.proxy.Interceptor} to be recognized as an aspect.
 *
 * This annotation is not retained at runtime. It is for use by tools to generate bundle manifests or
 * otherwise process the package.
 *
 * @see "Component Property Types"
 */
@Target(TYPE)
@Retention(CLASS)
@ComponentPropertyType
public @interface Aspect {

    String PREFIX_ = "service.aspect.";

    /**
     * The name of the aspect to provide. A class is used here to piggyback on Java's namespacing and
     * avoid conflicts in aspect names.
     *
     * @return The aspect class to provide
     */
    Class<?> name();

    /**
     * The extra properties that will be published, with value {@link Boolean#TRUE} to services woven
     * with this aspect. Defaults to the empty array.
     *
     * @return the extra properties
     */
    String[] extraProperties() default {};
}
