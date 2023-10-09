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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ServicePool<T> {

	private final Map<Object, T> originalToProxy = new IdentityHashMap<>();
	private final Map<T, Object> proxyToOriginal = new IdentityHashMap<>();
	private final Map<T, Integer> proxyToCount = new IdentityHashMap<>();

	public synchronized T get(final Object originalService, final Supplier<T> proxyFactory) {
		final T proxy = originalToProxy.computeIfAbsent(originalService, k -> proxyFactory.get());
		proxyToOriginal.putIfAbsent(proxy, originalService);
		proxyToCount.compute(proxy, (k, v) -> v == null ? 1 : v + 1);
		return proxy;
	}

	public synchronized boolean unget(final T proxy) {
		final Integer count = proxyToCount.compute(proxy, (k, v) -> v == null ? 0 : v - 1);
		if (count > 0) {
			return false;
		}
		// clean-up
		proxyToCount.remove(proxy);
		final Object original = proxyToOriginal.remove(proxy);
		final T proxyX = originalToProxy.remove(original);

		if (proxy != proxyX) {
			throw new IllegalStateException("Service Proxies are not the same");
		}
		return true;
	}

}
