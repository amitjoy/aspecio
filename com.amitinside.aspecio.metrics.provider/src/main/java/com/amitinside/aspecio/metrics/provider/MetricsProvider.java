/*******************************************************************************
 * Copyright 2022 Amit Kumar Mondal
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