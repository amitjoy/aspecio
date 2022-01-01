/*******************************************************************************
 * Copyright 2022 Amit Kumar Mondal
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
package com.amitinside.aspecio.metrics.aspect.provider;

import static com.amitinside.aspecio.metrics.aspect.api.MetricsAspect.MEASURED_PROPERTY;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;

import com.amitinside.aspecio.annotations.api.Aspect;
import com.amitinside.aspecio.metrics.annotation.api.Measured;
import com.amitinside.aspecio.metrics.api.Metrics;
import com.amitinside.aspecio.metrics.aspect.api.MetricsAspect;
import com.codahale.metrics.Timer.Context;

import io.primeval.reflex.proxy.CallContext;
import io.primeval.reflex.proxy.annotation.AnnotationInterceptor;
import io.primeval.reflex.proxy.handler.BooleanInterceptionHandler;
import io.primeval.reflex.proxy.handler.ByteInterceptionHandler;
import io.primeval.reflex.proxy.handler.CharInterceptionHandler;
import io.primeval.reflex.proxy.handler.DoubleInterceptionHandler;
import io.primeval.reflex.proxy.handler.FloatInterceptionHandler;
import io.primeval.reflex.proxy.handler.IntInterceptionHandler;
import io.primeval.reflex.proxy.handler.InterceptionHandler;
import io.primeval.reflex.proxy.handler.LongInterceptionHandler;
import io.primeval.reflex.proxy.handler.ShortInterceptionHandler;
import io.primeval.reflex.proxy.handler.VoidInterceptionHandler;

@Component
@Aspect(name = MetricsAspect.class, extraProperties = MEASURED_PROPERTY)
public final class AnnotatedMetricInterceptorImpl implements AnnotationInterceptor<Measured> {

    private final Metrics metrics;
    private final Map<CallContext, String> methodIds;

    @Activate
    public AnnotatedMetricInterceptorImpl(@Reference final Metrics metrics) {
        this.metrics = metrics;
        methodIds = new ConcurrentHashMap<>();
    }

    @Override
    public <T, E extends Throwable> T onCall(final Measured annotation, final CallContext callContext,
            final InterceptionHandler<T> handler) throws E {
        final String methodid = methodIds.computeIfAbsent(callContext, AnnotatedMetricInterceptorImpl::methodId);
        final Context syncTimer = metrics.timer(methodid).time();

        final boolean async = Promise.class.isAssignableFrom(callContext.method.getReturnType());

        try {
            final T result = handler.invoke();
            if (async) {
                final Context resolveTimer = metrics.timer(methodid + "::promise").time();
                final Promise<?> pms = (Promise<?>) result;
                pms.onResolve(resolveTimer::close);
            }
            return result;
        } finally {
            syncTimer.close();
        }
    }

    @Override
    public <E extends Throwable> boolean onCall(final Measured annotation, final CallContext context,
            final BooleanInterceptionHandler handler) throws E {
        final String methodid = methodIds.computeIfAbsent(context, AnnotatedMetricInterceptorImpl::methodId);
        final Context syncTimer = metrics.timer(methodid).time();
        try {
            return handler.invoke();
        } finally {
            syncTimer.close();
        }
    }

    @Override
    public <E extends Throwable> byte onCall(final Measured annotation, final CallContext context,
            final ByteInterceptionHandler handler) throws E {
        final String methodid = methodIds.computeIfAbsent(context, AnnotatedMetricInterceptorImpl::methodId);
        final Context syncTimer = metrics.timer(methodid).time();
        try {
            return handler.invoke();
        } finally {
            syncTimer.close();
        }
    }

    @Override
    public <E extends Throwable> char onCall(final Measured annotation, final CallContext context,
            final CharInterceptionHandler handler) throws E {
        final String methodid = methodIds.computeIfAbsent(context, AnnotatedMetricInterceptorImpl::methodId);
        final Context syncTimer = metrics.timer(methodid).time();
        try {
            return handler.invoke();
        } finally {
            syncTimer.close();
        }
    }

    @Override
    public <E extends Throwable> double onCall(final Measured annotation, final CallContext context,
            final DoubleInterceptionHandler handler) throws E {
        final String methodid = methodIds.computeIfAbsent(context, AnnotatedMetricInterceptorImpl::methodId);
        final Context syncTimer = metrics.timer(methodid).time();
        try {
            return handler.invoke();
        } finally {
            syncTimer.close();
        }
    }

    @Override
    public <E extends Throwable> float onCall(final Measured annotation, final CallContext context,
            final FloatInterceptionHandler handler) throws E {
        final String methodid = methodIds.computeIfAbsent(context, AnnotatedMetricInterceptorImpl::methodId);
        final Context syncTimer = metrics.timer(methodid).time();
        try {
            return handler.invoke();
        } finally {
            syncTimer.close();
        }
    }

    @Override
    public <E extends Throwable> int onCall(final Measured annotation, final CallContext context,
            final IntInterceptionHandler handler) throws E {
        final String methodid = methodIds.computeIfAbsent(context, AnnotatedMetricInterceptorImpl::methodId);
        final Context syncTimer = metrics.timer(methodid).time();
        try {
            return handler.invoke();
        } finally {
            syncTimer.close();
        }
    }

    @Override
    public <E extends Throwable> long onCall(final Measured annotation, final CallContext context,
            final LongInterceptionHandler handler) throws E {
        final String methodid = methodIds.computeIfAbsent(context, AnnotatedMetricInterceptorImpl::methodId);
        final Context syncTimer = metrics.timer(methodid).time();
        try {
            return handler.invoke();
        } finally {
            syncTimer.close();
        }
    }

    @Override
    public <E extends Throwable> short onCall(final Measured annotation, final CallContext context,
            final ShortInterceptionHandler handler) throws E {
        final String methodid = methodIds.computeIfAbsent(context, AnnotatedMetricInterceptorImpl::methodId);
        final Context syncTimer = metrics.timer(methodid).time();
        try {
            return handler.invoke();
        } finally {
            syncTimer.close();
        }
    }

    @Override
    public <E extends Throwable> void onCall(final Measured annotation, final CallContext context,
            final VoidInterceptionHandler handler) throws E {
        final String methodid = methodIds.computeIfAbsent(context, AnnotatedMetricInterceptorImpl::methodId);
        final Context syncTimer = metrics.timer(methodid).time();
        try {
            handler.invoke();
        } finally {
            syncTimer.close();
        }
    }

    public static String methodId(final CallContext cc) {
        final Method method = cc.method;
        // @formatter:off
        final String classAndMethodName =
                "."                                        +
                method.getDeclaringClass().getSimpleName() +
                "."                                        +
                method.getName()                           +
                "(" + cc.parameters.stream()
                                   .map(p -> p.getType().getSimpleName())
                                   .collect(joining(",")) + ")";

        return Stream.of(method.getDeclaringClass()
                               .getPackage()
                               .getName()
                               .split("\\."))
                     .map(s -> s.subSequence(0, 1))
                     .collect(joining(".", "", classAndMethodName));
        // @formatter:on
    }

    @Override
    public Class<Measured> intercept() {
        return Measured.class;
    }

    @Override
    public String toString() {
        return "MetricsInterceptor";
    }

}