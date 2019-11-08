package com.amitinside.aspecio.service;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ServicePool<T> {

  private final Map<Object, T>  originalToProxy = new IdentityHashMap<>();
  private final Map<T, Object>  proxyToOriginal = new IdentityHashMap<>();
  private final Map<T, Integer> proxyToCount    = new IdentityHashMap<>();

  public synchronized T get(final Object originalService, final Supplier<T> proxyFactory) {
    final T proxy = originalToProxy.computeIfAbsent(originalService, k -> proxyFactory.get());
    proxyToOriginal.putIfAbsent(proxy, originalService);
    proxyToCount.compute(proxy, (k, v) -> {
      if (v == null) {
        return 1;
      } else {
        return v + 1;
      }
    });
    return proxy;
  }

  public synchronized boolean unget(final T proxy) {
    final Integer count = proxyToCount.compute(proxy, (k, v) -> {
      if (v == null) {
        return 0;
      } else {
        return v - 1;
      }
    });

    if (count > 0) {
      return false;
    }
    // clean-up
    proxyToCount.remove(proxy);
    final Object original = proxyToOriginal.remove(proxy);
    final T      proxyX   = originalToProxy.remove(original);

    if (proxy != proxyX) {
      throw new RuntimeException("Service Proxies are not same");
    }

    return true;
  }

}
