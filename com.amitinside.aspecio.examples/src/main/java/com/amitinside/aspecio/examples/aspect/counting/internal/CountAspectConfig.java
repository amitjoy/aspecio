package com.amitinside.aspecio.examples.aspect.counting.internal;

public @interface CountAspectConfig {
  boolean countOnlySuccessful() default false;
}
