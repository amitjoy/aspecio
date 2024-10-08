/*******************************************************************************
 * Copyright 2022-2025 Amit Kumar Mondal
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

import java.util.stream.Stream;

public enum ServiceScope {

	/**
	 * When the component is registered as a service, it must be registered as a
	 * bundle scope service but only a single instance of the component must be used
	 * for all bundles using the service.
	 */
	SINGLETON("singleton"),

	/**
	 * When the component is registered as a service, it must be registered as a
	 * bundle scope service and an instance of the component must be created for
	 * each bundle using the service.
	 */
	BUNDLE("bundle"),

	/**
	 * When the component is registered as a service, it must be registered as a
	 * prototype scope service and an instance of the component must be created for
	 * each distinct request for the service.
	 */
	PROTOTYPE("prototype");

	private final String value;

	ServiceScope(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static ServiceScope fromString(final String scope) {
		return Stream.of(values()).filter(sc -> sc.value.equals(scope)).findAny().orElse(SINGLETON);
	}
}
