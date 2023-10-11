/*******************************************************************************
 * Copyright 2021-2023 Amit Kumar Mondal
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
package com.amitinside.aspecio.examples.command;

import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amitinside.aspecio.examples.aspect.counting.CountingAspect;
import com.amitinside.aspecio.examples.async.SuperSlowService;

@Component(service = TestGogoCommand.class)
@GogoCommand(scope = "test", function = { "showCounts", "callSlowService" })
public final class TestGogoCommand {

	@Reference
	private CountingAspect countingAspect;

	@Reference
	private SuperSlowService superSlowService;

	public void showCounts() {
		countingAspect.printCounts();
	}

	public void callSlowService() {
		superSlowService.compute();
	}

}
