/*******************************************************************************
 * Copyright 2021-2025 Amit Kumar Mondal
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
package com.amitinside.aspecio.api;

import java.util.List;
import java.util.Set;

import org.osgi.dto.DTO;

/**
 * The data transfer object describing a service candidate for interception by
 * Aspecio, along with the aspects it defines and its status.
 *
 * @NotThreadSafe
 */
public class InterceptedServiceDTO extends DTO {

	/** The unique identifier of the service. */
	public long serviceId;

	/** The unique identifier of the bundle. */
	public long bundleId;

	/** Indicates if the service is published. */
	public boolean published;

	/** The list of object classes implemented by the service. */
	public List<String> objectClass;

	/** The set of aspects that are satisfied for this service. */
	public Set<String> satisfiedAspects;

	/** The set of aspects that are required for this service. */
	public Set<String> requiredAspects;

	/** The set of optional aspects for this service. */
	public Set<String> optionalAspects;

	/** The set of required aspects that are not satisfied for this service. */
	public Set<String> unsatisfiedRequiredAspects;
}