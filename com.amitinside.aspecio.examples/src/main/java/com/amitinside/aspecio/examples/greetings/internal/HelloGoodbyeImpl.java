package com.amitinside.aspecio.examples.greetings.internal;

import org.osgi.service.component.annotations.Component;
import com.amitinside.aspecio.annotations.api.Weave;
import com.amitinside.aspecio.examples.aspect.counting.CountingAspect;
import com.amitinside.aspecio.examples.aspect.metric.MetricAspect;
import com.amitinside.aspecio.examples.greetings.Goodbye;
import com.amitinside.aspecio.examples.greetings.Hello;

@Component
@Weave(required = CountingAspect.class, optional = MetricAspect.All.class)
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
