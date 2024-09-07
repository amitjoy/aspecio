package com.amitinside.aspecio.service;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ServicePool<T> {

    private final Map<Object, T> originalToProxy = new IdentityHashMap<>();
    private final Map<T, Object> proxyToOriginal = new IdentityHashMap<>();
    private final Map<T, Integer> proxyToCount = new IdentityHashMap<>();

    public synchronized T get(final Object originalService, final Supplier<T> proxyFactory) {
        T proxy = originalToProxy.computeIfAbsent(originalService, k -> createNewProxy(originalService, proxyFactory));
        incrementProxyCount(proxy);
        return proxy;
    }

    private T createNewProxy(final Object originalService, final Supplier<T> proxyFactory) {
        T newProxy = proxyFactory.get();
        proxyToOriginal.put(newProxy, originalService);
        return newProxy;
    }

    private void incrementProxyCount(final T proxy) {
        proxyToCount.merge(proxy, 1, Integer::sum);
    }

    public synchronized boolean unget(final T proxy) {
        int count = decrementProxyCount(proxy);
        if (count > 0) {
            return false;
        }

        cleanUp(proxy);
        return true;
    }

    private int decrementProxyCount(final T proxy) {
        return proxyToCount.merge(proxy, -1, Integer::sum);
    }

    private void cleanUp(final T proxy) {
        proxyToCount.remove(proxy);
        Object original = proxyToOriginal.remove(proxy);
        T proxyX = originalToProxy.remove(original);

        if (proxy != proxyX) {
            throw new IllegalStateException("Service proxies do not match.");
        }
    }
}