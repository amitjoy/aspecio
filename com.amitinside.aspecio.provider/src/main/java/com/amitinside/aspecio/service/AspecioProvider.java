/*******************************************************************************
 * Copyright 2022-2024 Amit Kumar Mondal
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
package com.amitinside.aspecio.service;

import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_WEAVE;
import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_WEAVE_OPTIONAL;
import static org.osgi.namespace.service.ServiceNamespace.SERVICE_NAMESPACE;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.osgi.annotation.bundle.Capability;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.framework.hooks.service.ListenerHook.ListenerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.aspecio.api.Aspecio;
import com.amitinside.aspecio.api.AspectDTO;
import com.amitinside.aspecio.api.InterceptedServiceDTO;
import com.amitinside.aspecio.util.Exceptions;

@Capability(namespace = SERVICE_NAMESPACE, attribute = "objectClass:List<String>=com.amitinside.aspecio.api.Aspecio")
public final class AspecioProvider implements Aspecio, FindHook, EventListenerHook {

    private final Logger logger = LoggerFactory.getLogger(AspecioProvider.class);

    private final long bundleId;
    private final ServiceWeavingManager serviceWeavingManager;
    private final AspectInterceptorManager aspectInterceptorManager;
    private final AspecioServiceController aspecioServiceController;

    public AspecioProvider(final BundleContext bundleContext) {
        bundleId = bundleContext.getBundle().getBundleId();

        aspectInterceptorManager = new AspectInterceptorManager(bundleContext);
        serviceWeavingManager = new ServiceWeavingManager(bundleContext);
        aspecioServiceController = new AspecioServiceController(aspectInterceptorManager, serviceWeavingManager);
    }

    public void activate() {
        logger.info("Activating Aspecio");
        try {
            aspecioServiceController.open();
        } catch (final InvalidSyntaxException e) {
            throw Exceptions.duck(e);
        }
        logger.info("Aspecio activated");
    }

    public void deactivate() {
        aspecioServiceController.close();
        logger.info("Aspecio deactivated");
    }

    @Override
    public void event(final ServiceEvent event, final Map<BundleContext, Collection<ListenerInfo>> listeners) {
        // Is it an event we want to filter out?
        final ServiceReference<?> ref = event.getServiceReference();
        final Object weaveProperty = ref.getProperty(SERVICE_ASPECT_WEAVE);
        final Object optionalWeaveProperty = ref.getProperty(SERVICE_ASPECT_WEAVE_OPTIONAL);

        if (weaveProperty == null && optionalWeaveProperty == null) {
            return;
        }
        final Iterator<BundleContext> iterator = listeners.keySet().iterator();
        while (iterator.hasNext()) {
            final BundleContext consumingBundleContext = iterator.next();
            final long consumingBundleID = consumingBundleContext.getBundle().getBundleId();

            if (consumingBundleID == bundleId || consumingBundleID == 0) {
                continue; // allow self and system bundle
            }
            iterator.remove();
        }
    }

    @Override
    public void find(final BundleContext context, final String name, final String filter, final boolean allServices,
            final Collection<ServiceReference<?>> references) {
        final long consumingBundleId = context.getBundle().getBundleId();
        if (consumingBundleId == bundleId || consumingBundleId == 0) {
            return; // allow self and system bundle
        }
        final Iterator<ServiceReference<?>> iterator = references.iterator();
        while (iterator.hasNext()) {
            final ServiceReference<?> reference = iterator.next();
            final Object weaveProperty = reference.getProperty(SERVICE_ASPECT_WEAVE);
            final Object optionalWeaveProperty = reference.getProperty(SERVICE_ASPECT_WEAVE_OPTIONAL);

            if (weaveProperty == null && optionalWeaveProperty == null) {
                continue;
            }
            iterator.remove();
        }
    }

    @Override
    public Set<String> getRegisteredAspects() {
        return aspectInterceptorManager.getRegisteredAspects();
    }

    @Override
    public Optional<AspectDTO> getAspectDescription(final String aspectName) {
        return aspectInterceptorManager.getAspectDescription(aspectName);
    }

    @Override
    public List<InterceptedServiceDTO> getInterceptedServices() {
        return aspecioServiceController.getInterceptedServices();
    }
}