/*******************************************************************************
 * Copyright 2021-2024 Amit Kumar Mondal
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
package com.amitinside.aspecio.examples.greetings.internal;

import org.osgi.service.component.annotations.Component;

import com.amitinside.aspecio.annotations.api.Weave;
import com.amitinside.aspecio.examples.aspect.counting.CountingAspect;
import com.amitinside.aspecio.examples.aspect.metric.MetricAspect;
import com.amitinside.aspecio.examples.greetings.GoodBye;
import com.amitinside.aspecio.examples.greetings.Hello;

@Component
@Weave(required = CountingAspect.class, optional = MetricAspect.All.class)
public final class HelloGoodBye implements Hello, GoodBye {

	@Override
	public String hello() {
		return "hello";
	}

	@Override
	public String goodbye() {
		return "goodbye";
	}

}
