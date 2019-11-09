package com.amitinside.aspecio.service;

import static io.primeval.reflex.proxy.Interceptor.DEFAULT;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import io.primeval.reflex.proxy.Interceptor;
import io.primeval.reflex.proxy.bytecode.Proxy;

public final class AspecioServiceObject {

    private final ServiceScope            serviceScope;
    private final ServiceReference<?>     originalRef;
    private final Function<Object, Proxy> proxyFunction;
    private final List<Proxy>             instances = new CopyOnWriteArrayList<>();

    // Try to de-duplicate for service factories that are just lazy singletons.
    private final ServicePool<Proxy> servicePool = new ServicePool<>();
    private Object                   serviceToRegister;
    private volatile Interceptor     interceptor = DEFAULT;

    public AspecioServiceObject(final ServiceScope serviceScope, final ServiceReference<?> originalRef,
            final Function<Object, Proxy> proxyFunction) {
        this.serviceScope  = serviceScope;
        this.originalRef   = originalRef;
        this.proxyFunction = proxyFunction;
    }

    public void setInterceptor(final Interceptor interceptor) {
        this.interceptor = interceptor;
        for (final Proxy w : instances) {
            w.setInterceptor(interceptor);
        }
    }

    public synchronized Object getServiceObjectToRegister() {
        if (serviceToRegister == null) {
            serviceToRegister = makeServiceObjectToRegister();
        }
        return serviceToRegister;
    }

    private Object makeServiceObjectToRegister() {
        switch (serviceScope) {
            case PROTOTYPE:
                return new PrototypeServiceFactory<Proxy>() {
                    @Override
                    public Proxy getService(final Bundle bundle, final ServiceRegistration<Proxy> registration) {
                        final Object originalService = bundle.getBundleContext().getService(originalRef);
                        final Proxy  instance        = proxyFunction.apply(originalService);
                        instance.setInterceptor(interceptor);
                        instances.add(instance);
                        return instance;
                    }

                    @Override
                    public void ungetService(final Bundle bundle, final ServiceRegistration<Proxy> registration,
                            final Proxy service) {
                        instances.remove(service);
                        final BundleContext bundleContext = bundle.getBundleContext();
                        // If bundle is still there, let's unget, otherwise ignore.
                        if (bundleContext != null) {
                            bundleContext.ungetService(originalRef);
                        }
                    }
                };
            case BUNDLE:
                return new ServiceFactory<Proxy>() {
                    @Override
                    public Proxy getService(final Bundle bundle, final ServiceRegistration<Proxy> registration) {
                        final Object originalService = bundle.getBundleContext().getService(originalRef);
                        return servicePool.get(originalService, () -> {
                            final Proxy woven = proxyFunction.apply(originalService);
                            woven.setInterceptor(interceptor);
                            instances.add(woven);
                            return woven;
                        });
                    }

                    @Override
                    public void ungetService(final Bundle bundle, final ServiceRegistration<Proxy> registration,
                            final Proxy service) {
                        final boolean empty = servicePool.unget(service);
                        if (empty) {
                            instances.remove(service);
                        }
                        final BundleContext bundleContext = bundle.getBundleContext();
                        // If bundle is still there, let's unget, otherwise ignore.
                        if (bundleContext != null) {
                            bundleContext.ungetService(originalRef);
                        }
                    }
                };
            default:
                final Object originalService = originalRef.getBundle().getBundleContext().getService(originalRef);
                final Proxy instance = proxyFunction.apply(originalService);
                instance.setInterceptor(interceptor);
                instances.add(instance);
                return instance;
        }

    }
}
