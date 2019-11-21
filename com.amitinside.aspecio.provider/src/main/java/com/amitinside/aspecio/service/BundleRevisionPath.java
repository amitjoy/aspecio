package com.amitinside.aspecio.service;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.osgi.framework.wiring.BundleRevision;

import io.primeval.reflex.proxy.bytecode.ProxyClassLoader;

public final class BundleRevisionPath {

    private ProxyClassLoader                        classLoader;
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
