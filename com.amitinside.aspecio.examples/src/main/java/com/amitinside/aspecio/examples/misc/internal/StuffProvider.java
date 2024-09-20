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
package com.amitinside.aspecio.examples.misc.internal;

import java.io.PrintStream;
import java.util.stream.IntStream;

import org.osgi.service.component.annotations.Component;

import com.amitinside.aspecio.annotations.api.Weave;
import com.amitinside.aspecio.examples.aspect.metric.Timed;
import com.amitinside.aspecio.examples.misc.Stuff;

@Component
@Weave(required = Timed.class)
public final class StuffProvider implements Stuff {

	@Override
	public void test(final PrintStream ps, final int i, final byte b, final String s) {
		ps.println(s + " " + i + " b" + b);
	}

	@Override
	public double foo(final double a, final int[] b) {
		return a + IntStream.of(b).sum();
	}
}
