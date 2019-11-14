package com.amitinside.aspecio.examples.async.internal;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import com.amitinside.aspecio.annotations.api.Weave;
import com.amitinside.aspecio.examples.aspect.counting.CountingAspect;
import com.amitinside.aspecio.examples.aspect.metric.MetricAspect;
import com.amitinside.aspecio.examples.aspect.metric.Timed;
import com.amitinside.aspecio.examples.async.SuperSlowService;
import com.google.common.util.concurrent.Uninterruptibles;

@Component
@Weave(required = MetricAspect.AnnotatedOnly.class, optional = CountingAspect.class)
public final class SuperSlowServiceImpl implements SuperSlowService {

    private ExecutorService executor;
    private PromiseFactory  promiseFactory;

    @Activate
    public void activate() {
        executor       = Executors.newFixedThreadPool(3);
        promiseFactory = new PromiseFactory(executor);
    }

    @Deactivate
    public void deactivate() {
        executor.shutdown();
    }

    @Override
    @Timed
    public Promise<Long> compute() {
        final Deferred<Long> deferred = promiseFactory.deferred();
        executor.submit(() -> {
            Uninterruptibles.sleepUninterruptibly(3, SECONDS);
            deferred.resolve(42L);
        });
        return deferred.getPromise();
    }

}
