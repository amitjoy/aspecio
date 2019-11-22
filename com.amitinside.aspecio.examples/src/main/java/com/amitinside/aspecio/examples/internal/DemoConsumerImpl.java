package com.amitinside.aspecio.examples.internal;

import java.io.PrintStream;
import java.util.concurrent.Executors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import com.amitinside.aspecio.examples.DemoConsumer;
import com.amitinside.aspecio.examples.async.SuperSlowService;
import com.amitinside.aspecio.examples.greetings.Goodbye;
import com.amitinside.aspecio.examples.greetings.Hello;

@Component
public final class DemoConsumerImpl implements DemoConsumer {

    @Reference
    private Hello hello;

    @Reference
    private Goodbye goodbye;

    @Reference
    private SuperSlowService superSlowService;

    private final PromiseFactory promiseFactory;

    @Activate
    public DemoConsumerImpl() {
        promiseFactory = new PromiseFactory(Executors.newSingleThreadExecutor());
    }

    @Override
    public void consumeTo(final PrintStream out) {
        out.println(hello.hello() + " " + goodbye.goodbye());
    }

    @Override
    public Promise<Long> getLongResult() {
        final Deferred<Long> d       = promiseFactory.deferred();
        final Promise<Long>  promise = superSlowService.compute();
        promise.onResolve(() -> new Thread(() -> d.resolveWith(promise)).start());
        return d.getPromise();
    }

}
