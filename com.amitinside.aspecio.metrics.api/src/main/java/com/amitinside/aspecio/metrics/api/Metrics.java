/*******************************************************************************
 * Copyright 2021-2025 Amit Kumar Mondal
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
package com.amitinside.aspecio.metrics.api;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;

/**
 * Used to provide metrics data
 */
public interface Metrics extends MetricSet {

	/**
	 * Provides counter for the specified aspect
	 *
	 * @param name the name of the aspect
	 * @return the counter instance
	 */
	Counter counter(String name);

	/**
	 * Provides the timer for the specified aspect
	 *
	 * @param name the name of the aspect
	 * @return the timer instance
	 */
	Timer timer(String name);

	/**
	 * Provides the meter for the specified aspect
	 *
	 * @param name the name of the aspect
	 * @return the meter instance
	 */
	Meter meter(String name);

}