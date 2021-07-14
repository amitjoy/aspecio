package com.amitinside.aspecio.metrics.annotation.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * Annotation to be used to intercept metrics information from the annotated class
 */
@Retention(RUNTIME)
public @interface Measured {

}