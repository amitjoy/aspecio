package com.amitinside.aspecio.service;

import static com.amitinside.aspecio.service.WovenServiceEvent.OPTIONAL_ASPECT_CHANGE;
import static com.amitinside.aspecio.service.WovenServiceEvent.REQUIRED_ASPECT_CHANGE;
import static com.amitinside.aspecio.service.WovenServiceEvent.SERVICE_PROPERTIES_CHANGE;
import static com.amitinside.aspecio.util.AspecioUtil.copySet;
import static com.amitinside.aspecio.util.AspecioUtil.getLongValue;
import static org.osgi.framework.Constants.SERVICE_BUNDLEID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.Logger;

import com.amitinside.aspecio.api.InterceptedServiceDTO;
import com.amitinside.aspecio.logging.AspecioLogger;

public final class AspecioServiceController implements AspectInterceptorListener, WovenServiceListener {

    private final Logger logger = AspecioLogger.getLogger(AspecioServiceController.class);

    private final AspectInterceptorManager aspectInterceptorManager;
    private final ServiceWeavingManager    serviceWeavingManager;

    private final Map<ServiceReference<?>, ManagedWovenService> managedServices = new ConcurrentHashMap<>();

    public AspecioServiceController(final AspectInterceptorManager aspectInterceptorManager,
            final ServiceWeavingManager serviceWeavingManager) {
        this.aspectInterceptorManager = aspectInterceptorManager;
        this.serviceWeavingManager    = serviceWeavingManager;
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
                return;
            case SERVICE_UPDATE:
                handleServiceUpdate(wovenService, event.matchesCause(REQUIRED_ASPECT_CHANGE),
                        event.matchesCause(OPTIONAL_ASPECT_CHANGE), event.matchesCause(SERVICE_PROPERTIES_CHANGE));
                return;
            case SERVICE_DEPARTURE:
                handleServiceDeparture(wovenService);
                return;
        }
    }

    private synchronized void handleServiceArrival(final WovenService wovenService) {
        final ManagedWovenService managedWovenService = new ManagedWovenService();
        final ManagedWovenService old                 = managedServices.put(wovenService.originalReference,
                managedWovenService);
        if (old != null) {
            logger.warn("Got an old service that we shouldn't for service ID {}", wovenService.originalServiceId);
            old.unregister();
        }
        final AspectInterceptorContext context = aspectInterceptorManager.getContext(wovenService.requiredAspects,
                wovenService.optionalAspects);

        managedWovenService.wovenService  = wovenService;
        managedWovenService.aspectContext = context;
        managedWovenService.wovenService.aspecioServiceObject.setInterceptor(context.interceptor);

        final boolean satisfied = context.unsatisfiedRequiredAspects.isEmpty();
        if (satisfied) {
            managedWovenService.register();
        }
    }

    private synchronized void handleServiceUpdate(final WovenService wovenService, final boolean requiredAspectsChanged,
            final boolean optionalAspectsChanged, final boolean servicePropertiesChanged) {
        final ManagedWovenService managed = managedServices.get(wovenService.originalReference);
        if (managed == null) {
            logger.trace(
                    "Couldn't find an old service while we should for service ID {}, treating the update as a new service...(?)",
                    wovenService.originalServiceId);
            handleServiceArrival(wovenService);
            return;
        }
        if (requiredAspectsChanged || optionalAspectsChanged) {
            final AspectInterceptorContext context = aspectInterceptorManager.getContext(wovenService.requiredAspects,
                    wovenService.optionalAspects);
            managed.wovenService  = wovenService;
            managed.aspectContext = context;
            managed.wovenService.aspecioServiceObject.setInterceptor(context.interceptor);

            final boolean satisfied = context.unsatisfiedRequiredAspects.isEmpty();

            if (satisfied) {
                if (managed.registration == null) {
                    // newly satisfied!
                    managed.register();
                }
            } else {
                if (managed.registration != null) {
                    // was satisfied before, but not anymore...!
                    managed.unregister();
                }
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
            final long bundleId = getLongValue(mws.wovenService.originalReference.getProperty(SERVICE_BUNDLEID));

            final InterceptedServiceDTO dto = new InterceptedServiceDTO();
            dto.serviceId                  = mws.wovenService.originalServiceId;
            dto.bundleId                   = bundleId;
            dto.objectClass                = new ArrayList<>(mws.wovenService.objectClass);
            dto.published                  = mws.registration != null;
            dto.satisfiedAspects           = copySet(mws.aspectContext.satisfiedAspects);
            dto.unsatisfiedRequiredAspects = copySet(mws.aspectContext.unsatisfiedRequiredAspects);
            dto.requiredAspects            = copySet(mws.wovenService.requiredAspects);
            dto.optionalAspects            = copySet(mws.wovenService.optionalAspects);

            isds.add(dto);
        }
        return isds;
    }

}
