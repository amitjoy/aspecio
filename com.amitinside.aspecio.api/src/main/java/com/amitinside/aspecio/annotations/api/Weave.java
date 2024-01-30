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
 *
 * <ul>
 * <ul>
 * <li>{@link AspecioConstants#SERVICE_ASPECT_WEAVE}</li>
 * <li>{@link AspecioConstants#SERVICE_ASPECT_WEAVE_OPTIONAL}</li>
 * </ul>
 * </ul>
 *
 * <p>
 * This annotation can be used on a {@link Component} to request aspects to be
 * woven.
 *
 * This annotation is not retained at runtime. It is for use by tools to
 * generate service property in the component XML.
 *
 * @see "Component Property Types"
 */
@Target(TYPE)
@Retention(CLASS)
@ComponentPropertyType
public @interface Weave {

	String PREFIX_ = "service.aspect.weave.";

	/**
	 * The required aspects to weave. The woven service will not be published unless
	 * all of the required aspects are present. <br>
	 * The deregistration of any required aspect will also cause the woven service
	 * to be unregistered.
	 *
	 * @return the required aspects.
	 */
	Class<?>[] required() default {};

	/**
	 * The optional aspects to weave. The woven service will be published even if
	 * the optional aspects are absent. <br>
	 * The registration of an optional aspect will allow these aspects to intercept
	 * the service methods, even if the service was previously published without
	 * that aspect.
	 *
	 * @return the optional aspects.
	 */
	Class<?>[] optional() default {};

}
