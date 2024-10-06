/*******************************************************************************
 * Copyright 2022-2025 Amit Kumar Mondal
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

import static com.amitinside.aspecio.service.WovenServiceEvent.ChangeEvent.*;
import static com.amitinside.aspecio.util.AspecioUtil.*;
import static org.osgi.framework.Constants.SERVICE_BUNDLEID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amitinside.aspecio.api.InterceptedServiceDTO;

public final class AspecioServiceController implements AspectInterceptorListener, WovenServiceListener {

    private static final Logger logger = LoggerFactory.getLogger(AspecioServiceController.class);

    private final AspectInterceptorManager aspectInterceptorManager;
    private final ServiceWeavingManager serviceWeavingManager;

    private final Map<ServiceReference<?>, ManagedWovenService> managedServices = new ConcurrentHashMap<>();

    public AspecioServiceController(final AspectInterceptorManager aspectInterceptorManager,
                                    final ServiceWeavingManager serviceWeavingManager) {
        this.aspectInterceptorManager = aspectInterceptorManager;
        this.serviceWeavingManager = serviceWeavingManager;
    }

    public void open() throws InvalidSyntaxException {
        aspectInterceptorManager.addListener(this);
        serviceWeavingManager.addListener(this);
        aspectInterceptorManager.open();
        serviceWeavingManager.open();
    }

    public void close() {
        serviceWeavingManager.close();
        aspectInterceptorManager.close();
        aspectInterceptorManager.removeListener(this);
        serviceWeavingManager.removeListener(this);
    }

    @Override
    public void onAspectChange(final AspectInterceptorListener.EventKind eventKind, final String aspectName,
                               final AspectInterceptor aspectInterceptor) {
        final List<WovenService> wovenServicesForAspect = serviceWeavingManager.getWovenServicesForAspect(aspectName);
        if (wovenServicesForAspect == null) {
            return;
        }
        for (final WovenService wovenService : wovenServicesForAspect) {
            final boolean required = wovenService.requiredAspects.contains(aspectName);
            handleServiceUpdate(wovenService, required, !required, false);
        }
    }

    @Override
    public void onWovenServiceEvent(final WovenServiceEvent event, final WovenService wovenService) {
        switch (event.kind) {
            case SERVICE_ARRIVAL:
                handleServiceArrival(wovenService);
                break;
            case SERVICE_UPDATE:
                handleServiceUpdate(wovenService, event.matchesCause(REQUIRED_ASPECT_CHANGE),
                        event.matchesCause(OPTIONAL_ASPECT_CHANGE), event.matchesCause(SERVICE_PROPERTIES_CHANGE));
                break;
            case SERVICE_DEPARTURE:
                handleServiceDeparture(wovenService);
                break;
            default:
                break;
        }
    }

    private synchronized void handleServiceArrival(final WovenService wovenService) {
        final ManagedWovenService managedWovenService = new ManagedWovenService();
        final ManagedWovenService old = managedServices.put(wovenService.originalReference, managedWovenService);
        if (old != null) {
            logger.warn("Got an old service with service ID {}", wovenService.originalServiceId);
            old.unregister();
        }
        final AspectInterceptorContext context = aspectInterceptorManager.getContext(wovenService.requiredAspects,
                                                                                     wovenService.optionalAspects);

        managedWovenService.wovenService = wovenService;
        managedWovenService.aspectContext = context;
        managedWovenService.wovenService.aspecioServiceObject.setInterceptor(context.getInterceptor());

        if (context.getUnsatisfiedRequiredAspects().isEmpty()) {
            managedWovenService.register();
        }
    }

    private synchronized void handleServiceUpdate(final WovenService wovenService, final boolean requiredAspectsChanged,
                                                  final boolean optionalAspectsChanged, final boolean servicePropertiesChanged) {
        final ManagedWovenService managed = managedServices.get(wovenService.originalReference);
        if (managed == null) {
            logger.trace("Couldn't find an old service with service ID {}, treating the update as a new service...",
                         wovenService.originalServiceId);
            handleServiceArrival(wovenService);
            return;
        }

        if (requiredAspectsChanged || optionalAspectsChanged) {
            final AspectInterceptorContext context = aspectInterceptorManager.getContext(wovenService.requiredAspects,
                                                                                         wovenService.optionalAspects);
            managed.wovenService = wovenService;
            managed.aspectContext = context;
            managed.wovenService.aspecioServiceObject.setInterceptor(context.getInterceptor());

            final boolean satisfied = context.getUnsatisfiedRequiredAspects().isEmpty();
            if (satisfied) {
                if (managed.registration == null) {
                    managed.register();
                }
            } else if (managed.registration != null) {
                managed.unregister();
            }
        } else if (servicePropertiesChanged) {
            managed.wovenService = wovenService;
            managed.registration.setProperties(managed.getProperties());
        }
    }

    private synchronized void handleServiceDeparture(final WovenService wovenService) {
        final ManagedWovenService managed = managedServices.remove(wovenService.originalReference);
        if (managed == null) {
            logger.warn("Notified of the departure of a service we couldn't find with service ID {}",
                        wovenService.originalServiceId);
            return;
        }
        managed.unregister();
    }

    public synchronized List<InterceptedServiceDTO> getInterceptedServices() {
        final List<InterceptedServiceDTO> isds = new ArrayList<>(managedServices.size());
        for (final ManagedWovenService mws : managedServices.values()) {
            final long bundleId = asLong(mws.wovenService.originalReference.getProperty(SERVICE_BUNDLEID));

            final InterceptedServiceDTO dto = new InterceptedServiceDTO();
            dto.serviceId = mws.wovenService.originalServiceId;
            dto.bundleId = bundleId;
            dto.objectClass = new ArrayList<>(mws.wovenService.objectClass);
            dto.published = mws.registration != null;
            dto.satisfiedAspects = asSet(mws.aspectContext.getSatisfiedAspects());
            dto.unsatisfiedRequiredAspects = asSet(mws.aspectContext.getUnsatisfiedRequiredAspects());
            dto.requiredAspects = asSet(mws.wovenService.requiredAspects);
            dto.optionalAspects = asSet(mws.wovenService.optionalAspects);

            isds.add(dto);
        }
        return isds;
    }
}