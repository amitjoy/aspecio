package com.amitinside.aspecio.examples.misc.internal;

import java.io.PrintStream;
import java.util.stream.IntStream;
import org.osgi.service.component.annotations.Component;
import com.amitinside.aspecio.annotations.api.Weave;
import com.amitinside.aspecio.examples.aspect.metric.Timed;
import com.amitinside.aspecio.examples.misc.Stuff;

@Component
@Weave(required = Timed.class)
public final class StuffImpl implements Stuff {
  @Override
  public void test(final PrintStream ps, final int i, final byte b, final String s) {
    ps.println(s + " " + i + " b" + b);
  }

  @Override
  public double foo(final double a, final int[] b) {
    return a + IntStream.of(b).sum();
  }
}
