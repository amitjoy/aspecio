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
package com.amitinside.aspecio.examples.aspect.counting.internal;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import com.amitinside.aspecio.annotations.api.Aspect;
import com.amitinside.aspecio.examples.aspect.counting.CountingAspect;

import io.primeval.reflex.proxy.CallContext;
import io.primeval.reflex.proxy.Interceptor;
import io.primeval.reflex.proxy.handler.InterceptionHandler;

@Component
@Aspect(name = CountingAspect.class)
public final class CountingAspectImpl implements Interceptor, CountingAspect {

	@interface CountAspectConfig {
		boolean countOnlySuccessful() default false;
	}

	private final Map<Method, Integer> methodCallCount = new ConcurrentHashMap<>();

	private volatile boolean countOnlySuccessful = false;

	@Activate
	public void activate(final CountAspectConfig config) {
		countOnlySuccessful = config.countOnlySuccessful();
	}

	@Deactivate
	public void deactivate() {
		methodCallCount.clear();
	}

	@Modified
	public void modified(final CountAspectConfig config) {
		countOnlySuccessful = config.countOnlySuccessful();
	}

	@Override
	public <T, E extends Throwable> T onCall(final CallContext context, final InterceptionHandler<T> handler) throws E {
		if (countOnlySuccessful) {
			final T res = handler.invoke();
			methodCallCount.compute(context.method, (k, v) -> v == null ? 1 : (v += 1));
			return res;
		}
		methodCallCount.compute(context.method, (k, v) -> v == null ? 1 : (v += 1));
		return handler.invoke();
	}

	@Override
	public void printCounts() {
		methodCallCount.forEach((m, count) -> System.out
				.println(m.getDeclaringClass().getName() + "::" + m.getName() + " -> " + count));
	}

	@Override
	public String toString() {
		return "Counting";
	}

}
