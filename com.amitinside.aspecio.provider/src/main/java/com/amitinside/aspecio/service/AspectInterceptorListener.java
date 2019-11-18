package com.amitinside.aspecio.service;

@FunctionalInterface
public interface AspectInterceptorListener {

    enum EventKind {
        NEW_MATCH,
        NO_MATCH
    }

    void onAspectChange(EventKind eventKind, String aspectName, AspectInterceptor aspectInterceptor);
}
