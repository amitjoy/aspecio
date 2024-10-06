/*******************************************************************************
 * Copyright 2022-2025 Amit Kumar Mondal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

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

    private final ServiceScope serviceScope;
    private final ServiceReference<?> originalRef;
    private final Function<Object, Proxy> proxyFunction;
    private final List<Proxy> instances = new CopyOnWriteArrayList<>();
    private final ServicePool<Proxy> servicePool = new ServicePool<>();
    private Object serviceToRegister;
    private volatile Interceptor interceptor = DEFAULT;

    public AspecioServiceObject(final ServiceScope serviceScope, final ServiceReference<?> originalRef,
                                final Function<Object, Proxy> proxyFunction) {
        this.serviceScope = serviceScope;
        this.originalRef = originalRef;
        this.proxyFunction = proxyFunction;
    }

    public void setInterceptor(final Interceptor interceptor) {
        this.interceptor = interceptor;
        instances.forEach(proxy -> proxy.setInterceptor(interceptor));
    }

    public synchronized Object getServiceObjectToRegister() {
        if (serviceToRegister == null) {
            serviceToRegister = createServiceObjectToRegister();
        }
        return serviceToRegister;
    }

    private Object createServiceObjectToRegister() {
        switch (serviceScope) {
            case PROTOTYPE:
                return createPrototypeServiceFactory();
            case BUNDLE:
                return createBundleServiceFactory();
            default:
                return createDefaultServiceObject();
        }
    }

    private PrototypeServiceFactory<Proxy> createPrototypeServiceFactory() {
        return new PrototypeServiceFactory<Proxy>() {
            @Override
            public Proxy getService(final Bundle bundle, final ServiceRegistration<Proxy> registration) {
                final Object originalService = bundle.getBundleContext().getService(originalRef);
                final Proxy instance = proxyFunction.apply(originalService);
                instance.setInterceptor(interceptor);
                instances.add(instance);
                return instance;
            }

            @Override
            public void ungetService(final Bundle bundle, final ServiceRegistration<Proxy> registration,
                                     final Proxy service) {
                instances.remove(service);
                ungetOriginalService(bundle);
            }
        };
    }

    private ServiceFactory<Proxy> createBundleServiceFactory() {
        return new ServiceFactory<Proxy>() {
            @Override
            public Proxy getService(final Bundle bundle, final ServiceRegistration<Proxy> registration) {
                final Object originalService = bundle.getBundleContext().getService(originalRef);
                return servicePool.get(originalService, () -> {
                    final Proxy proxy = proxyFunction.apply(originalService);
                    proxy.setInterceptor(interceptor);
                    instances.add(proxy);
                    return proxy;
                });
            }

            @Override
            public void ungetService(final Bundle bundle, final ServiceRegistration<Proxy> registration,
                                     final Proxy service) {
                if (servicePool.unget(service)) {
                    instances.remove(service);
                }
                ungetOriginalService(bundle);
            }
        };
    }

    private Object createDefaultServiceObject() {
        final Object originalService = originalRef.getBundle().getBundleContext().getService(originalRef);
        final Proxy instance = proxyFunction.apply(originalService);
        instance.setInterceptor(interceptor);
        instances.add(instance);
        return instance;
    }

    private void ungetOriginalService(final Bundle bundle) {
        final BundleContext bundleContext = bundle.getBundleContext();
        if (bundleContext != null) {
            bundleContext.ungetService(originalRef);
        }
    }
}