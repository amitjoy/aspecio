/*******************************************************************************
 * Copyright 2021-2025 Amit Kumar Mondal
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
package com.amitinside.aspecio.api;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.Constants;

/**
 * <p>
 * <b>Aspecio Service Interface</b><br>
 * This interface is primarily designed for building a monitoring tool to obtain 
 * information about Aspecio's activities.
 * </p>
 *
 * <p>
 * It is important to note that this class does not allow changes to Aspecio's state. 
 * It provides a read-only view.
 * </p>
 *
 * <p>
 * To define and update aspects, a service object implementing 
 * {@code io.primeval.reflex.proxy.Interceptor} must be registered in the OSGi 
 * service registry with the {@link String} property 
 * {@link AspecioConstants#SERVICE_ASPECT} set to the name of the aspect.
 * </p>
 *
 * <p>
 * An aspect service may specify the property 
 * {@link AspecioConstants#SERVICE_ASPECT_EXTRAPROPERTIES} of type 
 * {@link String} or String[] to register additional OSGi properties with the 
 * services it is woven into. For example, if an aspect defines an extra service 
 * property named {@code secure}, services woven with that aspect will be 
 * published with the OSGi property {@code secure} set to {@code true}. This can 
 * be used by consuming code to ensure that a service exhibits certain behavior 
 * (in this case, ensuring it is secured, either by the aspect or custom code).
 * </p>
 *
 * <p>
 * Aspecio will <i>weave</i> aspects into OSGi services that define the service 
 * properties {@link AspecioConstants#SERVICE_ASPECT_WEAVE} (for required 
 * aspects) or {@link AspecioConstants#SERVICE_ASPECT_WEAVE_OPTIONAL}. 
 * <br/><br/>
 * By default, if a required aspect is not present, the original service will 
 * not be available until a service providing the required aspect is registered. 
 * To change this behavior and disable the filtering of services, use the 
 * framework property {@link AspecioConstants#ASPECIO_FILTER_SERVICES} and set 
 * it to {@code false}. 
 * <br/><br/>
 * Changing this property via Java system properties will only take effect after 
 * restarting the Aspecio bundle. Note that due to the inability to publish a 
 * previously filtered service, you should also restart bundles providing woven 
 * services after changing this property.
 * </p>
 */
@ProviderType
public interface Aspecio {

    /**
     * Get the set of Aspects currently registered as seen by Aspecio.
     * The returned set is a view and changing it will not change the state of Aspecio.
     *
     * @return The set containing the registered Aspect names.
     */
    Set<String> getRegisteredAspects();

    /**
     * Get the {@link AspectDTO} for Aspect named {@literal aspectName}, or {@link Optional#empty()} if there is no such Aspect.
     *
     * @param aspectName The name of the aspect (case sensitive)
     * @return An Optional containing the matching {@link AspectDTO}, or {@link Optional#empty()}
     * @throws NullPointerException if the {@code aspectName} is {@code null}
     */
    Optional<AspectDTO> getAspectDescription(String aspectName);

    /**
     * Get the list of {@link InterceptedServiceDTO}, as seen by Aspecio.
     *
     * @return The list of {@link InterceptedServiceDTO}, or an empty list if there are no intercepted services.
     */
    List<InterceptedServiceDTO> getInterceptedServices();

    /**
     * Get the list of {@link InterceptedServiceDTO}, as seen by Aspecio, filtered by objectClass.
     *
     * @param objectClassContains A filter that must be part of the {@link Constants#OBJECTCLASS} OSGi property of the intercepted service to be selected.
     * @return The list of {@link InterceptedServiceDTO}, or an empty list if there are no intercepted services.
     */
    default List<InterceptedServiceDTO> getInterceptedServices(final String objectClassContains) {
        final List<InterceptedServiceDTO> interceptedServices = getInterceptedServices();
        final Iterator<InterceptedServiceDTO> iterator = interceptedServices.iterator();
        entryLoop:
        while (iterator.hasNext()) {
            final InterceptedServiceDTO serviceDescription = iterator.next();
            for (final String objClass : serviceDescription.objectClass) {
                if (objClass.contains(objectClassContains)) {
                    continue entryLoop;
                }
            }
            iterator.remove();
        }
        return interceptedServices;
    }
}