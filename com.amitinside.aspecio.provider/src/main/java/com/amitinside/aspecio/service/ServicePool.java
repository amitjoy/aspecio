/*******************************************************************************
 * Copyright 2022-2024 Amit Kumar Mondal
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
package com.amitinside.aspecio.service;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ServicePool<T> {

	private final Map<Object, T> originalToProxy = new IdentityHashMap<>();
	private final Map<T, Object> proxyToOriginal = new IdentityHashMap<>();
	private final Map<T, Integer> proxyToCount = new IdentityHashMap<>();

	public synchronized T get(final Object originalService, final Supplier<T> proxyFactory) {
		T proxy = originalToProxy.computeIfAbsent(originalService, k -> proxyFactory.get());
		proxyToOriginal.putIfAbsent(proxy, originalService);
		proxyToCount.merge(proxy, 1, Integer::sum);
		return proxy;
	}

	public synchronized boolean unget(final T proxy) {
		int count = proxyToCount.merge(proxy, -1, Integer::sum);
		if (count > 0) {
			return false;
		}
		// Clean-up
		proxyToCount.remove(proxy);
		Object original = proxyToOriginal.remove(proxy);
		T proxyX = originalToProxy.remove(original);

		if (proxy != proxyX) {
			throw new IllegalStateException("Service proxies do not match.");
		}
		return true;
	}
}