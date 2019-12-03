package com.amitinside.aspecio.metrics.aspect.api;

public final class MetricsAspect {

    private MetricsAspect() {
        throw new IllegalAccessError("Cannot be instantiated");
    }

    public static final String MEASURED_PROPERTY = "aspecio.metrics.measured";

}