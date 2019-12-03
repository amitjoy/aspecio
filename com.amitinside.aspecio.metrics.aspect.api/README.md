# Aspecio Metrics

Uses Aspecio to provide an aspect named `MetricsAspect`. It measures the execution time of methods annotated with the `@Measured` annotation.


# Bundles to be installed

1. `com.amitinside.aspecio.metrics.provider`
2. `com.amitinside.aspecio.metrics.aspect.provider`


# Requirements

1. Java 8
2. `com.amitinside.aspecio.provider`


# Aspects provided

Aspect `MetricsAspect` is provided.

When active, this aspects uses a Metrics `timer` to time execution time of methods annotated with the `@Measured` annotation.

The timer is named from the method identifier: package names are short-form (initials only), exceptions and return types are ignored. For instance, a method identifier might look as `c.a.a.e.a.m.i.AnnotatedMetricInterceptorImpl.intercept()`.

Counters under these names represent the time before a method returns (normally or exceptionally). 

If a method returns an OSGi `Promise`, then another timer is used and has the form `<methodId>::promise`. It represents the time until a `Promise` is resolved (successfully or not). A method returning a Promise should ideally be non-blocking and have negligible execution time, while the promise resolution time should display actual performance.

Also note that a service property named `aspecio.metrics.measured` will be set to `Boolean.TRUE` to your service if it is properly woven with that aspect. 

# Adding MetricsAspect to your service

To add MetricsAspect to your service, set your OSGi service property `service.aspect.weave.optional` (or `service.aspect.weave` if you want mandatory weaving) to `io.primeval.metrics.aspect.MetricsAspect`. Don't forget your implementation must add `@Measured` to intercepted methods you wish to measure (Aspecio and Primeval Metrics must be active).

If you use Declarative Services, use the annotations from `com.amitinside.aspecio.annotations.api` package:

```java
@Component
@Weave(optional = MetricsAspect.class)
public final class HelloGoodbyeImpl implements Hello, Goodbye {

    @Measured
    public String hello() {
        return "hello";
    }

    public String goodbye() {
        return "goodbye";
    }

}
```