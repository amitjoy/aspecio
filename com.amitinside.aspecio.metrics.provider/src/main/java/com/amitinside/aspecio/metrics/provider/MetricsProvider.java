package com.amitinside.aspecio.metrics.provider;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

import com.amitinside.aspecio.metrics.api.Metrics;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

@Component
public final class MetricsProvider implements Metrics {

    private final MetricRegistry metricRegistry = new MetricRegistry();

    @Override
    public Map<String, Metric> getMetrics() {
        return metricRegistry.getMetrics();
    }

    @Override
    public Counter counter(final String name) {
        return metricRegistry.counter(name);
    }

    @Override
    public Timer timer(final String name) {
        return metricRegistry.timer(name);
    }

    @Override
    public Meter meter(final String name) {
        return metricRegistry.meter(name);
    }
}