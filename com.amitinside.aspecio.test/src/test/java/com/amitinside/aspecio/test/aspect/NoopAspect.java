package com.amitinside.aspecio.test.aspect;

import io.primeval.reflex.proxy.CallContext;
import io.primeval.reflex.proxy.Interceptor;
import io.primeval.reflex.proxy.handler.InterceptionHandler;

public class NoopAspect implements Interceptor {

    @Override
    public <T, E extends Throwable> T onCall(final CallContext context, final InterceptionHandler<T> handler) throws E {
        return handler.invoke();
    }

}