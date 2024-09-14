/*******************************************************************************
 * Copyright 2021-2025 Amit Kumar Mondal
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

import java.util.Set;

import org.osgi.dto.DTO;

/**
 * The data transfer object describing an interceptor as seen by Aspecio.
 * This class provides information about the interceptor's service and bundle IDs, 
 * service ranking, class, and any additional properties associated with the interceptor.
 *
 * @NotThreadSafe
 */
public class InterceptorDTO extends DTO {

    /** The service ID of the interceptor */
    public long serviceId;

    /** The bundle ID of the interceptor */
    public long bundleId;

    /** The service ranking of the interceptor */
    public int serviceRanking;

    /** The class of the interceptor */
    public Class<?> interceptorClass;

    /** A set of extra properties related to the interceptor */
    public Set<String> extraProperties;
}