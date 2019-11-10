package com.amitinside.aspecio.service;

import java.util.stream.Stream;

public enum ServiceScope {

    /**
     * When the component is registered as a service, it must be registered as a
     * bundle scope service but only a single instance of the component must be
     * used for all bundles using the service.
     */
    SINGLETON("singleton"),

    /**
     * When the component is registered as a service, it must be registered as a
     * bundle scope service and an instance of the component must be created for
     * each bundle using the service.
     */
    BUNDLE("bundle"),

    /**
     * When the component is registered as a service, it must be registered as a
     * prototype scope service and an instance of the component must be created
     * for each distinct request for the service.
     */
    PROTOTYPE("prototype");

    private final String value;

    ServiceScope(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceScope fromString(final String scope) {
        return Stream.of(values()).filter(sc -> sc.value.equals(scope)).findAny().orElse(SINGLETON);
    }
}
