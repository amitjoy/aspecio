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
import java.util.Set;

import org.osgi.dto.DTO;

/**
 * The data transfer object describing a service candidate to interception by
 * Aspecio, along with the Aspects it define and its status.
 *
 * @NotThreadSafe
 */
public class InterceptedServiceDTO extends DTO {

	public long serviceId;
	public long bundleId;
	public boolean published;
	public List<String> objectClass;
	public Set<String> satisfiedAspects;
	public Set<String> requiredAspects;
	public Set<String> optionalAspects;
	public Set<String> unsatisfiedRequiredAspects;

}
