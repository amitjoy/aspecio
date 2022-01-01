/*******************************************************************************
 * Copyright 2022 Amit Kumar Mondal
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

import java.util.Objects;
import java.util.Set;

import org.osgi.framework.ServiceReference;

import io.primeval.reflex.proxy.Interceptor;

public final class AspectInterceptor implements Comparable<AspectInterceptor> {

    public final String aspect;
    public final Interceptor interceptor;
    public final ServiceReference<?> serviceRef;
    public final int serviceRanking;
    public final Set<String> extraProperties;

    public AspectInterceptor(final String aspect, final Interceptor interceptor, final ServiceReference<?> serviceRef,
            final int serviceRanking, final Set<String> extraProperties) {
        this.aspect = aspect;
        this.interceptor = interceptor;
        this.serviceRef = serviceRef;
        this.serviceRanking = serviceRanking;
        this.extraProperties = extraProperties;
    }

    @Override
    public int compareTo(final AspectInterceptor o) {
        return serviceRef.compareTo(o.serviceRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceRef);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AspectInterceptor)) {
            return false;
        }
        final AspectInterceptor other = (AspectInterceptor) obj;
        return Objects.equals(serviceRef, other.serviceRef);
    }

}
