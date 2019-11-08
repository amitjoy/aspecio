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
  public <T, E extends Throwable> T onCall(final CallContext callContext,
      final InterceptionHandler<T> handler) throws E {
    final Stopwatch started    = Stopwatch.createStarted();
    final String    methodName = callContext.target.getName() + "::" + callContext.method.getName();

    final boolean async = callContext.method.getReturnType() == Promise.class;

    try {
      final T result = handler.invoke();
      if (async) {
        final Promise<?> p = (Promise<?>) result;
        p.onResolve(() -> System.out.println("Async call to " + methodName + " took "
            + started.elapsed(TimeUnit.MICROSECONDS) + " µs"));
      }
      return result;
    } finally {
      System.out.println(
          "Sync call to " + methodName + " took " + started.elapsed(TimeUnit.MICROSECONDS) + " µs");
    }
  }

  @Override
  public String toString() {
    return "MetricsInterceptor:ALL";
  }
}
