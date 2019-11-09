package com.amitinside.aspecio.service;

import java.util.Set;
import io.primeval.reflex.proxy.Interceptor;

public final class AspectInterceptorContext {

    public final Interceptor interceptor;
    public final Set<String> satisfiedAspects;
    public final Set<String> satisfiedRequiredAspects;
    public final Set<String> unsatisfiedRequiredAspects;
    public final Set<String> satisfiedOptionalAspects;
    public final Set<String> unsatisfiedOptionalAspects;
    public final Set<String> extraProperties;

    public AspectInterceptorContext(final Interceptor interceptor, final Set<String> satisfiedAspects,
            final Set<String> satisfiedRequiredAspects, final Set<String> unsatisfiedRequiredAspects,
            final Set<String> satisfiedOptionalAspects, final Set<String> unsatisfiedOptionalAspects,
            final Set<String> extraProperties) {
        this.interceptor                = interceptor;
        this.satisfiedAspects           = satisfiedAspects;
        this.satisfiedRequiredAspects   = satisfiedRequiredAspects;
        this.unsatisfiedRequiredAspects = unsatisfiedRequiredAspects;
        this.satisfiedOptionalAspects   = satisfiedOptionalAspects;
        this.unsatisfiedOptionalAspects = unsatisfiedOptionalAspects;
        this.extraProperties            = extraProperties;
    }

}
