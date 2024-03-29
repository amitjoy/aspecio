<p align='center'>
    <img width="350" alt="logo" src="https://user-images.githubusercontent.com/13380182/69403424-d8fc6b80-0cfa-11ea-888d-cbb9b64acc68.png">
</p>

# Aspect Oriented Programming (AOP) Proxies for OSGi Services ![Build Status](https://travis-ci.org/amitjoy/aspecio.svg?branch=master)

Aspecio is a `micro-framework` providing `AOP Proxies` in OSGi environment. It brings a mix of component-oriented and aspect-oriented programming to your OSGi application. Aspecio lets you define `Aspects` that you can later pick to add behavior to your service components and avoid duplicating boilerplate dealing with cross-cutting concerns.

## Aspecio 1.0.0

`Aspecio 1.0.0` is the complete overhaul with a completely new proxy model that is simpler, completely unrestricted and faster. Refer to [Primeval Reflex](http://github.com/primeval-io/primeval-reflex) for the proxy capabilities.

## Documentation

In addition to this README, the API javadoc provides an extensive overview.

## Requirements

1. Java 8+
2. OSGi R7

## Overview

### Why Aspects?

In general, aspects enable you to intercept code and alter its execution behavior. However, there are several downsides to using aspects:

* Scattering behavior across the codebase
* Making the execution model opaque by having aspects intercept any random piece of code, including internal code that might have implicit invariants that aspects break
* Not knowing **which** aspects are being _woven_ on a piece of code at a given time
* Having some aspect framework implementations _weave_ aspects into one big bytecode muddy-ball, making debugging difficult when line numbers are desynchronized, adding synthetic methods in the bytecode
* Sometimes aspects are implemented using Java proxies which can break consuming code, for example, code relying on reflection such as annotation-driven frameworks

However, there are _cross-cutting concerns_ for which aspects can be beneficial, for example:

* **Security**: ensuring some conditions are met before being allowed into a function ; 
* **Metrics**: having live metrics on critical components (e.g. using Coda Hale's Metrics library) ;
* Ensuring a piece of code takes place in a transaction;
* And many more :-)

Aspecio aims to make aspects predictable and bridges them with the OSGi service registry model.

### Aspecio and OSGi

While Aspecio's internal weaving code can be interesting to plug into other Dependency Injection frameworks, it currently does support OSGi R7 exclusively out of the box.

Aspecio works with OSGi services and can weave any _willing_ OSGi service. 

Aspecio works with any service scope, `singleton`, `bundle` and `prototype` and will only create as many instances as expected. In case of service frameworks using the `bundle` scope to make service creation lazy (such as Declarative Services), but still having effectively `singleton` services, Aspecio will make sure each service instance has precisely one proxy instance.

Thanks to relying on OSGi's low-level service primitives, Aspecio can work with any OSGi service component framework, including any compliant implementation of Declarative Services, Blueprint, Guice + Peaberry, Apache Felix iPojo or Apache Felix Dependency Manager.

Aspecio has been tested on Felix 6.0.3 but should work with any framework compliant with OSGi R7.

In the following examples, Declarative Services (DS) is used.


### Aspecio's Weaving

Aspecio does select service objects that ask for certain aspects in their service properties, hiding (by default) the original service from all bundles except the system bundle and Aspecio itself.

Aspecio uses [Primeval Reflex](http://github.com/primeval-io/primeval-reflex) to proxy services requesting weaving. All interfaces and public methods are proxied.

See [Primeval Reflex](http://github.com/primeval-io/primeval-reflex) for documentation on the proxies and writing interceptors.

### Drawback

OSGi services registered as simple classes cannot be woven using Aspecio. The services need to implement well-defined exported service APIs or interfaces.

### Installing Aspecio in an OSGi Framework

Install `com.amitinside.aspecio.provider` to your OSGi framework, and it will work right away.

If there are already registered services with the weaving property, Aspecio will restart their bundles to make sure it has the opportunity to apply its service hook.

Aspecio first collects the set of bundles providing services to weave, sorts them using the natural `Bundle` comparator (based on bundle IDs). It stops them all in that order, then starts them all. The ordering should allow to keep the original installation order, and stopping and starting them by batch is aiming to minimize service level interactions between these bundles (such as re-creating components with static references too many times).


## Defining an aspect with Aspecio

In Aspecio, we use Java to declare an Aspect.

Here is a simple Aspect counting how many times a method has been invoked. Depending on its configuration, it may count only successful calls (e.g., methods that did not throw an exception) or all methods indiscriminately. 

```java
@Component
@Aspect(name = CountingAspect.class)
public final class CountingAspectImpl implements Interceptor {

    private final Map<Method, Integer> methodCallCount = new ConcurrentHashMap();
    
    // This could be dynamically configured using Declarative Service + ConfigAdmin
    private volatile boolean countOnlySuccessful = false;

    @Override
    public <T, E extends Throwable> T onCall(CallContext context, InterceptionHandler<T> handler) throws E {
        if (countOnlySuccessful) {
            T res = handler.invoke();
            methodCallCount.compute(context.method, (k, v) -> v == null ? 1 : (v += 1));
            return res;
        } else {
            methodCallCount.compute(context.method, (k, v) -> v == null ? 1 : (v += 1));
            return handler.invoke();
        }
    }    
}
```

Aspecio finds aspects by:

* Looking for OSGi services; in the example above, provided using the `@Component` Declarative Service annotation)
* That provide the OSGi service string property `AspecioConstants.SERVICE_ASPECT` (`"service.aspect.name"`) declared using the `@Aspect` annotation.
* That implements the interface `io.primeval.reflect.proxy.Interceptor` (it need not be provided as the service's `"objectClass"`).
* If several services provide the same aspect, Aspecio will pick the one with the highest-service ranking; in case of equal service rankings, Aspecio will pick the one with the lowest service ID. Aspecio supports OSGi's service dynamics and will gladly replace or update Aspects' lifecycles. Aspecio is always 'greedy': if a "better" interceptor is registered for a given aspect, all the services using it will have it updated immediately. 

In the example above, the component `CountingAspectImpl` provides the aspect named `"CountingAspect"` (a Java String). You can call your aspects with any string, but it is practical to use Java classes to piggyback on the namespaces. 

For documentation on Interceptors, see [Primeval Reflect](http://github.com/primeval-io/primeval-reflect).


## Aspect Weaving with Aspecio

In Aspecio, we can only weave OSGi services that opt-in to one or several aspects. This is because services have a well-defined contract and make it the perfect entry point for aspects.

Services must declare the OSGi service `String` or `String[]` property `service.aspect.weave.required` (for required aspects) or `"service.aspect.weave.optional"` (for optional aspects), with the aspect names as value, to be candidate for weaving.

### Example

Here is a Declarative Services component that will be woven by Aspecio using our `CountingAspect` declared earlier.

```java
@Component
@Weave(required = CountingAspect.class, optional = AnotherOptionalAspect.class)
public final class HelloGoodbyeImpl implements Hello, Goodbye {

    @Override
    public String hello() {
        return "hello";
    }

    @Override
    public String goodbye() {
        return "goodbye";
    }

}
```

That's all! Now any aspect woven will be notified with the calls of methods `hello()` or `goodbye()` and may interact by returning other values, throwing exceptions, catching exceptions, accessing the arguments of each call (or just some) or even update the arguments before the call takes place.

Also, because `"CountingAspect.class"` is `required` by `HelloGoodbyeImpl`, the service will **not** be visible until a service providing Aspect `"CountingAspect.class"` is available. All the kinds of OSGi dynamics can happen here: the aspect can be registered after a service requiring it or later.

Having `"AnotherOptionalAspect.class"` as an optional aspect will not prevent Aspecio's proxy of `HelloGoodbyeImpl` of being registered even in case `"AnotherOptionalAspect.class"` is missing; however, if it becomes available during `HelloGoodbyImpl`'s lifetime, it will start intercepting its methods as well.


## Aspect Patterns

### Annotation-based Interception

* When you want to intercept only specific annotated methods, and you can use the annotation to pass configuration to the interceptor
* When you annotate specific method parameters to guide your aspect

```java
@Component
@Aspect(provides = MyAnnotationDrivenAspect.class)
public final class MyAnnotationDrivenAspectImpl implements AnnotationInterceptor<MyAnnotation> {

    @Override
    public <T, E extends Throwable> T onCall(MyAnnotationDrivenAspect annotation, CallContext context,
                                                              InterceptionHandler<T> handler) throws E {
        // may contain previous info on how to use the aspect.
        ...
    }
    
    @Override
    public Class<MyAnnotation> intercept() {
        return MyAnnotation.class;
    }
}
```


See `AnnotationInterceptor` in [Primeval Reflex](http://github.com/primeval-io/primeval-reflex).


### Aspects bridging services

Because we rarely want the actual cross-cutting behaviour to reside in our interceptor, it is a better approach to use your favourite component framework to make your aspects merely bring a functionality provided elsewhere:


```java
@Component
@Aspect(provides = MyFeatureAspect.class)
public final class MyFeatureAspectImpl implements Interceptor {

    @Reference
    private MyFeature myFeatureService; // logic is in another service

    @Override
    public <T, E extends Throwable> T onCall(MyAnnotationDrivenAspect annotation, CallContext context,
                                                              InterceptionHandler<T> handler) throws E {
       // use MyFeature service
       ....
    }                                                              
}
```

### Interceptors registering extra service properties


```java
@Component
@Aspect(provides = MySecurityAspect.class, extraProperties = "secured")
public final class MySecurityAspectImpl implements Interceptor {

    @Reference private Auth auth;
    @Override
    public <T, E extends Throwable> T onCall(MyAnnotationDrivenAspect annotation, CallContext context,
                                                              InterceptionHandler<T> handler) throws E {
         auth.checkPermissions(...);
         ...
    }   
}
```

The proxy service object registered by Aspecio will have the OSGi service boolean property `"secured"` set to `Boolean.TRUE`. Now consuming code can check for that property using a target filter to know if a service is secure. The consuming code doesn't need to know whether a service was obtained manually or using an aspect.


## Debugging Aspecio

Aspecio provides a service aptly named `Aspecio` that can show you what Aspecio sees in runtime:

* which aspects are available
* which services are woven

Aspecio provides two Gogo commands to get the same information in the Gogo shell, `aspecio:aspects` and `aspecio:woven`.

Here's a sample output of these two commands:

```
g! aspects
* com.amitinside.aspecio.examples.aspect.metric.MetricAspect$All
  [ --- ACTIVE --- ] Service ID 29, class com.amitinside.aspecio.examples.aspect.metric.internal.AllMetricInterceptorImpl, extra properties: [measured]
                     Provided by: com.amitinside.aspecio.examples 1.0.0.201911130609 [10]

* com.amitinside.aspecio.examples.aspect.counting.CountingAspect
  [ --- ACTIVE --- ] Service ID 28, class com.amitinside.aspecio.examples.aspect.counting.internal.CountingAspectImpl, extra properties: []
                     Provided by: com.amitinside.aspecio.examples 1.0.0.201911130609 [10]

* com.amitinside.aspecio.examples.aspect.metric.MetricAspect$AnnotatedOnly
  [ --- ACTIVE --- ] Service ID 30, class com.amitinside.aspecio.examples.aspect.metric.internal.AnnotatedMetricInterceptorImpl, extra properties: [measured]
                     Provided by: com.amitinside.aspecio.examples 1.0.0.201911130609 [10]

g! woven
[0] Service ID: 34, objectClass: [com.amitinside.aspecio.examples.greetings.Hello, com.amitinside.aspecio.examples.greetings.Goodbye]
    Required Aspects: [com.amitinside.aspecio.examples.aspect.counting.CountingAspect], Optional Aspects: [com.amitinside.aspecio.examples.aspect.metric.MetricAspect$All]
    Provided by: com.amitinside.aspecio.examples 1.0.0.201911130609 [10]
    Satisfied: true
    Active Aspects: [com.amitinside.aspecio.examples.aspect.metric.MetricAspect$All, com.amitinside.aspecio.examples.aspect.counting.CountingAspect]

[1] Service ID: 37, objectClass: [com.amitinside.aspecio.examples.misc.Stuff]
    Required Aspects: [com.amitinside.aspecio.examples.aspect.metric.Timed], Optional Aspects: []
    Provided by: com.amitinside.aspecio.examples 1.0.0.201911130609 [10]
    Satisfied: false
    Missing Required Aspects: [com.amitinside.aspecio.examples.aspect.metric.Timed]

[2] Service ID: 31, objectClass: [com.amitinside.aspecio.examples.async.SuperSlowService]
    Required Aspects: [com.amitinside.aspecio.examples.aspect.metric.MetricAspect$AnnotatedOnly], Optional Aspects: [com.amitinside.aspecio.examples.aspect.counting.CountingAspect]
    Provided by: com.amitinside.aspecio.examples 1.0.0.201911130609 [10]
    Satisfied: true
    Active Aspects: [com.amitinside.aspecio.examples.aspect.counting.CountingAspect, com.amitinside.aspecio.examples.aspect.metric.MetricAspect$AnnotatedOnly]

g!
```

## Project Import

**Import as Eclipse Projects**

1. Install bndtools
2. Import all the projects (`Eclipse Menu -> File -> Import -> General -> Existing Projects into Workspace`)


## Building from Source

Run `./gradlew clean build` in the project root directory.

# Credits

Simon Chemouil has contributed to the development of the [initial version](https://github.com/primeval-io/aspecio). Due to inactivity, this version has been forked by Amit Kumar Mondal, making it OSGi R7 compatible and providing several fixes and enhancements to the source code.
