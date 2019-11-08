package com.amitinside.aspecio.examples.aspect.counting.internal;

import java.lang.reflect.Method;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import com.amitinside.aspecio.annotations.api.Aspect;
import com.amitinside.aspecio.examples.aspect.counting.CountingAspect;
import com.google.common.collect.Maps;
import io.primeval.reflex.proxy.CallContext;
import io.primeval.reflex.proxy.Interceptor;
import io.primeval.reflex.proxy.handler.InterceptionHandler;

@Component
@Aspect(name = CountingAspect.class)
public final class CountingAspectImpl implements Interceptor, CountingAspect {

  private final Map<Method, Integer> methodCallCount = Maps.newConcurrentMap();

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
  public <T, E extends Throwable> T onCall(final CallContext context,
      final InterceptionHandler<T> handler) throws E {
    if (countOnlySuccessful) {
      final T res = handler.invoke();
      methodCallCount.compute(context.method, (k, v) -> v == null ? 1 : (v += 1));
      return res;
    } else {
      methodCallCount.compute(context.method, (k, v) -> v == null ? 1 : (v += 1));
      return handler.invoke();
    }
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
