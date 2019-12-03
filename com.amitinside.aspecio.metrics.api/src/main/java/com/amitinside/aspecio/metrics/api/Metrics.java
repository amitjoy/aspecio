package com.amitinside.aspecio.metrics.api;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;

public interface Metrics extends MetricSet {

    Counter counter(String name);

    Timer timer(String name);

    Meter meter(String name);

}