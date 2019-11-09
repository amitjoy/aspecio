package com.amitinside.aspecio.examples.internal;

import java.io.PrintStream;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import com.amitinside.aspecio.examples.DemoConsumer;
import com.amitinside.aspecio.examples.async.SuperSlowService;
import com.amitinside.aspecio.examples.greetings.Goodbye;
import com.amitinside.aspecio.examples.greetings.Hello;

@Component
public final class DemoConsumerImpl implements DemoConsumer {

    private Hello            hello;
    private Goodbye          goodbye;
    private SuperSlowService superSlowService;

    @Override
    public void consumeTo(final PrintStream out) {
        try {
            out.println(hello.hello() + " " + goodbye.goodbye());
        } catch (final Throwable e) {
        }
    }

    @Override
    public Promise<Long> getLongResult() {
        final Deferred<Long> d = new Deferred<>();

        final Promise<Long> promise = superSlowService.compute();
        promise.onResolve(() -> {
            new Thread(() -> d.resolveWith(promise)).start();
        });
        final Promise<Long> promise2 = d.getPromise();

        return promise2;
    }

    @Reference
    public void setHello(final Hello hello) {
        this.hello = hello;
    }

    @Reference
    public void setGoodbye(final Goodbye goodbye) {
        this.goodbye = goodbye;
    }

    @Reference
    public void setSuperSlowService(final SuperSlowService superSlowService) {
        this.superSlowService = superSlowService;
    }

}
