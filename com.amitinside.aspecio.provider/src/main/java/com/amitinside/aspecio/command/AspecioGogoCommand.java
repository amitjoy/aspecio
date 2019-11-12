package com.amitinside.aspecio.command;

import java.util.List;
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.amitinside.aspecio.api.Aspecio;
import com.amitinside.aspecio.api.AspectDTO;
import com.amitinside.aspecio.api.InterceptedServiceDTO;
import com.amitinside.aspecio.api.InterceptorDTO;

public final class AspecioGogoCommand {

    private final BundleContext bundleContext;
    private final Aspecio       aspecio;

    public AspecioGogoCommand(final BundleContext bundleContext, final Aspecio aspecio) {
        this.bundleContext = bundleContext;
        this.aspecio       = aspecio;
    }

    // Gogo command "aspect:aspects"
    public void aspects() {
        for (final String aspectName : aspecio.getRegisteredAspects()) {
            System.out.println("* " + aspectName);
            final Optional<AspectDTO> aspectDescription = aspecio.getAspectDescription(aspectName);
            if (!aspectDescription.isPresent()) {
                System.out.println(" ...?! Err, that aspect just went away!");
                continue;
            }
            final AspectDTO description = aspectDescription.get();
            printInterceptorDescription("[ --- ACTIVE --- ]", description.interceptor);
            int i = 1;
            for (final InterceptorDTO id : description.backupInterceptors) {
                printInterceptorDescription("[ alternative #" + i + " ]", id);
                i++;
            }
        }
    }

    private void printInterceptorDescription(final String marker, final InterceptorDTO interceptorDescription) {
        final String shift = String.format("%" + (marker.length() + 3) + "s", "");
        System.out.println("  " + marker + " Service ID " + interceptorDescription.serviceId + ", class "
                + interceptorDescription.interceptorClass.getName() + ", extra properties: "
                + interceptorDescription.extraProperties);
        final long   serviceBundleId = interceptorDescription.bundleId;
        final Bundle bundle          = bundleContext.getBundle(serviceBundleId);
        System.out.println(shift + "Provided by: " + bundle.getSymbolicName() + " " + bundle.getVersion() + " ["
                + serviceBundleId + "]");
        System.out.println();
    }

    // Gogo command "aspect:woven"
    public void woven() {
        final List<InterceptedServiceDTO> interceptedServices = aspecio.getInterceptedServices();
        printWoven(interceptedServices);
    }

    public void woven(final String objectClass) {
        final List<InterceptedServiceDTO> interceptedServices = aspecio.getInterceptedServices(objectClass);
        printWoven(interceptedServices);
    }

    private void printWoven(final List<InterceptedServiceDTO> interceptedServices) {
        int          i         = 0;
        final int    shiftSize = 4 + interceptedServices.size() / 10;
        final String shift     = String.format("%" + shiftSize + "s", "");
        for (final InterceptedServiceDTO mws : interceptedServices) {
            System.out.println("[" + i + "] Service ID: " + mws.serviceId + ", objectClass: " + mws.objectClass);
            System.out.println(
                    shift + "Required Aspects: " + mws.requiredAspects + ", Optional Aspects: " + mws.optionalAspects);
            final long   serviceBundleId = mws.bundleId;
            final Bundle bundle          = bundleContext.getBundle(serviceBundleId);
            System.out.println(shift + "Provided by: " + bundle.getSymbolicName() + " " + bundle.getVersion() + " ["
                    + serviceBundleId + "]");
            final boolean satisfied = mws.published;
            System.out.println(shift + "Satisfied: " + satisfied);
            if (!satisfied) {
                System.out.println(shift + "Missing Required Aspects: " + mws.unsatisfiedRequiredAspects);
            } else {
                System.out.println(shift + "Active Aspects: " + mws.satisfiedAspects);
            }
            System.out.println();
            i++;
        }
    }

}
