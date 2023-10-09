/*******************************************************************************
 * Copyright 2022-2023 Amit Kumar Mondal
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

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.osgi.framework.wiring.BundleRevision;

import io.primeval.reflex.proxy.bytecode.ProxyClassLoader;

public final class BundleRevisionPath {

	private ProxyClassLoader classLoader;
	private Map<BundleRevision, BundleRevisionPath> subMap;

	public synchronized ProxyClassLoader computeClassLoaderIfAbsent(
			final Supplier<ProxyClassLoader> classLoaderSupplier) {
		classLoader = Optional.ofNullable(classLoader).orElse(classLoaderSupplier.get());
		return classLoader;
	}

	public synchronized Map<BundleRevision, BundleRevisionPath> computeSubMapIfAbsent(
			final Supplier<Map<BundleRevision, BundleRevisionPath>> subMapSupplier) {
		subMap = Optional.ofNullable(subMap).orElse(subMapSupplier.get());
		return subMap;
	}

}
