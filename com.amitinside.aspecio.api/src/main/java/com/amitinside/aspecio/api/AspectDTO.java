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
package com.amitinside.aspecio.api;

import java.util.List;

import org.osgi.dto.DTO;

/**
 * The data transfer object of an aspect and the interceptors providing it.
 *
 * @NotThreadSafe
 */
public class AspectDTO extends DTO {

	/**
	 * The aspect name.
	 */
	public String aspectName;

	/**
	 * The currently chosen interceptor for that aspect
	 */
	public InterceptorDTO interceptor;

	/**
	 * The sorted list of interceptors that will replace the current interceptor if
	 * the chosen interceptor is unregistered.
	 *
	 * <p>
	 * Interceptors are chosen using by comparing their ServiceReference, so a
	 * higher service ranking is preferred, and in the case of equal service
	 * rankings a lower service id will be chosen.
	 */
	public List<InterceptorDTO> backupInterceptors;

}
