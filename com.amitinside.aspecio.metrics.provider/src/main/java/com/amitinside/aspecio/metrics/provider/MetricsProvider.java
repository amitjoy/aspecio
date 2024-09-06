/*******************************************************************************
 * Copyright 2022-2024 Amit Kumar Mondal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.amitinside.aspecio.metrics.provider;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.amitinside.aspecio.metrics.api.Metrics;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * A provider for metrics, implementing the {@link Metrics} interface.
 * This class uses the Codahale Metrics library to manage and provide various types
 * of metrics such as counters, timers, and meters.
 */
@Component
public final class MetricsProvider implements Metrics {

    /** The metric registry used to manage metrics. */
    private final MetricRegistry metricRegistry;

    /**
     * Constructor that initializes the metric registry.
     * This method is automatically invoked upon the component's activation.
     */
    @Activate
    public MetricsProvider() {
        metricRegistry = new MetricRegistry();
    }

    /**
     * Retrieves all registered metrics.
     *
     * @return a map containing all registered metrics, keyed by their names.
     */
    @Override
    public Map<String, Metric> getMetrics() {
        return metricRegistry.getMetrics();
    }

    /**
     * Retrieves or creates a counter metric with the given name.
     *
     * @param name the name of the counter.
     * @return a {@link Counter} metric.
     */
    @Override
    public Counter counter(final String name) {
        return metricRegistry.counter(name);
    }

    /**
     * Retrieves or creates a timer metric with the given name.
     *
     * @param name the name of the timer.
     * @return a {@link Timer} metric.
     */
    @Override
    public Timer timer(final String name) {
        return metricRegistry.timer(name);
    }

    /**
     * Retrieves or creates a meter metric with the given name.
     *
     * @param name the name of the meter.
     * @return a {@link Meter} metric.
     */
    @Override
    public Meter meter(final String name) {
        return metricRegistry.meter(name);
    }
}