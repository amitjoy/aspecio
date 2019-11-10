package com.amitinside.aspecio.service;

@FunctionalInterface
public interface AspectInterceptorListener {

    enum EventKind {
        NEWMATCH,
        NOMATCH
    }

    void onAspectChange(EventKind eventKind, String aspectName, AspectInterceptor aspectInterceptor);
}
