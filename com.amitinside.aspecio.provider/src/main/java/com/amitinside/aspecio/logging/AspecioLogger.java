package com.amitinside.aspecio.logging;

import static java.util.Objects.requireNonNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LoggerFactory;

/**
 * Internal logger to be used in order to avoid a mandatory dependency on OSGi Logger. It first
 * tries to log to a OSGi log service implementation if there is one available and then fallback to
 * System out/err in case there is no log service available.
 */
public final class AspecioLogger {

  /** The bundle context */
  private final BundleContext bundleContext;

  /** The reference to the log service */
  private ServiceReference<LoggerFactory> loggerFactoryRef;

  /** The {@link LoggerFactory} instance */
  private LoggerFactory loggerFactory;

  /** The singleton logger */
  private static AspecioLogger singleton;

  /** Used in case the {@link LoggerFactory} is not available */
  private static final org.osgi.service.log.Logger NULL_LOGGER = new NullLogger();

  private AspecioLogger(final BundleContext bundleContext) {
    this.bundleContext = bundleContext;

    loggerFactoryRef = bundleContext.getServiceReference(LoggerFactory.class);
    if (loggerFactoryRef != null) {
      loggerFactory = bundleContext.getService(loggerFactoryRef);
    }
  }

  public static void init(final BundleContext bundleContext) {
    synchronized (AspecioLogger.class) {
      if (singleton != null) {
        return;
      }
      singleton = new AspecioLogger(bundleContext);
    }
  }

  /**
   * Performs cleanup
   */
  public static void destroy() {
    synchronized (AspecioLogger.class) {
      if (singleton != null) {
        singleton.destroyInternal();
        singleton = null;
      }
    }
  }

  /**
   * Instance cleanup
   */
  private void destroyInternal() {
    if (loggerFactoryRef != null) {
      bundleContext.ungetService(loggerFactoryRef);
      loggerFactoryRef = null;
      loggerFactory    = null;
    }
  }

  /**
   * Returns the logger for bundles to write messages to the log using SLF4J-style format strings
   *
   * @param clazz the class to use for the logger name (cannot be {@code null})
   * @return a logger to use (cannot be {@code null})
   */
  public static org.osgi.service.log.Logger getLogger(final Class<?> clazz) {
    requireNonNull(clazz, "'clazz' cannot be null");

    synchronized (AspecioLogger.class) {
      final Bundle bundle = FrameworkUtil.getBundle(AspecioLogger.class);
      if ((bundle.getState() & Bundle.ACTIVE) != Bundle.ACTIVE
          && (bundle.getState() & Bundle.RESOLVED) != Bundle.RESOLVED
          && (bundle.getState() & Bundle.STARTING) != Bundle.STARTING) {
        return NULL_LOGGER;
      }
      if (singleton != null && singleton.loggerFactory != null) {
        return singleton.loggerFactory.getLogger(clazz);
      }
    }
    return NULL_LOGGER;
  }
}
