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
    private final Aspecio aspecio;

    public AspecioGogoCommand(final BundleContext bundleContext, final Aspecio aspecio) {
        this.bundleContext = bundleContext;
        this.aspecio = aspecio;
    }

    // Gogo command "aspecio:aspects"
    public String aspects() {
        StringBuilder output = new StringBuilder();
        for (final String aspectName : aspecio.getRegisteredAspects()) {
            output.append("* ").append(aspectName).append("\n");
            final Optional<AspectDTO> aspectDescription = aspecio.getAspectDescription(aspectName);
            if (!aspectDescription.isPresent()) {
                output.append(" ...?! Err, that aspect just went away!\n");
                continue;
            }
            final AspectDTO description = aspectDescription.get();
            output.append(printInterceptorDescription("[ --- ACTIVE --- ]", description.interceptor));
            int i = 1;
            for (final InterceptorDTO id : description.backupInterceptors) {
                output.append(printInterceptorDescription("[ alternative #" + i + " ]", id));
                i++;
            }
        }
        return output.toString();
    }

    private String printInterceptorDescription(final String marker, final InterceptorDTO interceptorDescription) {
        StringBuilder output = new StringBuilder();
        final String shift = String.format("%" + (marker.length() + 3) + "s", "");
        output.append("  ").append(marker).append(" Service ID ").append(interceptorDescription.serviceId)
              .append(", class ").append(interceptorDescription.interceptorClass.getName())
              .append(", extra properties: ").append(interceptorDescription.extraProperties).append("\n");
        final long serviceBundleId = interceptorDescription.bundleId;
        final Bundle bundle = bundleContext.getBundle(serviceBundleId);
        output.append(shift).append("Provided by: ").append(bundle.getSymbolicName()).append(" ")
              .append(bundle.getVersion()).append(" [").append(serviceBundleId).append("]\n\n");
        return output.toString();
    }

    // Gogo command "aspecio:woven"
    public String woven() {
        final List<InterceptedServiceDTO> interceptedServices = aspecio.getInterceptedServices();
        return printWoven(interceptedServices);
    }

    public String woven(final String objectClass) {
        final List<InterceptedServiceDTO> interceptedServices = aspecio.getInterceptedServices(objectClass);
        return printWoven(interceptedServices);
    }

    private String printWoven(final List<InterceptedServiceDTO> interceptedServices) {
        StringBuilder output = new StringBuilder();
        int i = 0;
        final int shiftSize = 4 + interceptedServices.size() / 10;
        final String shift = String.format("%" + shiftSize + "s", "");
        for (final InterceptedServiceDTO mws : interceptedServices) {
            output.append("[").append(i).append("] Service ID: ").append(mws.serviceId)
                  .append(", objectClass: ").append(mws.objectClass).append("\n");
            output.append(shift).append("Required Aspects: ").append(mws.requiredAspects)
                  .append(", Optional Aspects: ").append(mws.optionalAspects).append("\n");
            final long serviceBundleId = mws.bundleId;
            final Bundle bundle = bundleContext.getBundle(serviceBundleId);
            output.append(shift).append("Provided by: ").append(bundle.getSymbolicName()).append(" ")
                  .append(bundle.getVersion()).append(" [").append(serviceBundleId).append("]\n");
            final boolean satisfied = mws.published;
            output.append(shift).append("Satisfied: ").append(satisfied).append("\n");
            if (!satisfied) {
                output.append(shift).append("Missing Required Aspects: ").append(mws.unsatisfiedRequiredAspects).append("\n");
            } else {
                output.append(shift).append("Active Aspects: ").append(mws.satisfiedAspects).append("\n");
            }
            output.append("\n");
            i++;
        }
        return output.toString();
    }
}