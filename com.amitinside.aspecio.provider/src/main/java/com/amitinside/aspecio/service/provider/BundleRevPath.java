package com.amitinside.aspecio.service.provider;

import java.util.Map;
import java.util.function.Supplier;
import org.osgi.framework.wiring.BundleRevision;
import io.primeval.reflex.proxy.bytecode.ProxyClassLoader;

public final class BundleRevPath {

  private ProxyClassLoader                   classLoader;
  private Map<BundleRevision, BundleRevPath> subMap;

  public synchronized ProxyClassLoader computeClassLoaderIfAbsent(
      final Supplier<ProxyClassLoader> classLoaderSupplier) {
    if (classLoader == null) {
      classLoader = classLoaderSupplier.get();
    }
    return classLoader;
  }

  public synchronized Map<BundleRevision, BundleRevPath> computeSubMapIfAbsent(
      final Supplier<Map<BundleRevision, BundleRevPath>> subMapSupplier) {
    if (subMap == null) {
      subMap = subMapSupplier.get();
    }
    return subMap;
  }

}
