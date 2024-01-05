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
package com.amitinside.aspecio.examples.aspect.metric.internal;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;
import org.osgi.util.promise.Promise;

import com.amitinside.aspecio.annotations.api.Aspect;
import com.amitinside.aspecio.examples.aspect.metric.MetricAspect;
import com.google.common.base.Stopwatch;

import io.primeval.reflex.proxy.CallContext;
import io.primeval.reflex.proxy.Interceptor;
import io.primeval.reflex.proxy.handler.InterceptionHandler;

@Component
@Aspect(name = MetricAspect.All.class, extraProperties = "measured")
public final class AllMetricInterceptorImpl implements Interceptor {

	@Override
	public <T, E extends Throwable> T onCall(final CallContext callContext, final InterceptionHandler<T> handler)
			throws E {
		final Stopwatch started = Stopwatch.createStarted();
		final String methodName = callContext.target.getName() + "::" + callContext.method.getName();

		final boolean async = callContext.method.getReturnType() == Promise.class;

		try {
			final T result = handler.invoke();
			if (async) {
				final Promise<?> p = (Promise<?>) result;
				p.onResolve(() -> System.out.println(
						"Async call to " + methodName + " took " + started.elapsed(TimeUnit.MICROSECONDS) + " µs"));
			}
			return result;
		} finally {
			System.out
					.println("Sync call to " + methodName + " took " + started.elapsed(TimeUnit.MICROSECONDS) + " µs");
		}
	}

	@Override
	public String toString() {
		return "MetricsInterceptor:ALL";
	}
}
