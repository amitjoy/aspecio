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

import java.util.Collections;
import java.util.Set;

import io.primeval.reflex.proxy.Interceptor;

public final class AspectInterceptorContext {

    private final Interceptor interceptor;
    private final Set<String> satisfiedAspects;
    private final Set<String> satisfiedRequiredAspects;
    private final Set<String> unsatisfiedRequiredAspects;
    private final Set<String> satisfiedOptionalAspects;
    private final Set<String> unsatisfiedOptionalAspects;
    private final Set<String> extraProperties;

    public AspectInterceptorContext(
        final Interceptor interceptor,
        final Set<String> satisfiedAspects,
        final Set<String> satisfiedRequiredAspects,
        final Set<String> unsatisfiedRequiredAspects,
        final Set<String> satisfiedOptionalAspects,
        final Set<String> unsatisfiedOptionalAspects,
        final Set<String> extraProperties) {
        
        this.interceptor = interceptor;
        this.satisfiedAspects = Collections.unmodifiableSet(satisfiedAspects);
        this.satisfiedRequiredAspects = Collections.unmodifiableSet(satisfiedRequiredAspects);
        this.unsatisfiedRequiredAspects = Collections.unmodifiableSet(unsatisfiedRequiredAspects);
        this.satisfiedOptionalAspects = Collections.unmodifiableSet(satisfiedOptionalAspects);
        this.unsatisfiedOptionalAspects = Collections.unmodifiableSet(unsatisfiedOptionalAspects);
        this.extraProperties = Collections.unmodifiableSet(extraProperties);
    }

    public Interceptor getInterceptor() {
        return interceptor;
    }

    public Set<String> getSatisfiedAspects() {
        return satisfiedAspects;
    }

    public Set<String> getSatisfiedRequiredAspects() {
        return satisfiedRequiredAspects;
    }

    public Set<String> getUnsatisfiedRequiredAspects() {
        return unsatisfiedRequiredAspects;
    }

    public Set<String> getSatisfiedOptionalAspects() {
        return satisfiedOptionalAspects;
    }

    public Set<String> getUnsatisfiedOptionalAspects() {
        return unsatisfiedOptionalAspects;
    }

    public Set<String> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public String toString() {
        return "AspectInterceptorContext{" +
                "interceptor=" + interceptor +
                ", satisfiedAspects=" + satisfiedAspects +
                ", satisfiedRequiredAspects=" + satisfiedRequiredAspects +
                ", unsatisfiedRequiredAspects=" + unsatisfiedRequiredAspects +
                ", satisfiedOptionalAspects=" + satisfiedOptionalAspects +
                ", unsatisfiedOptionalAspects=" + unsatisfiedOptionalAspects +
                ", extraProperties=" + extraProperties +
                '}';
    }
}