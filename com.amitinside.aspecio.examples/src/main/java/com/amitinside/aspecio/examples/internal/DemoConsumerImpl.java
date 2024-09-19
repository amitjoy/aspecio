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
package com.amitinside.aspecio.examples.internal;

import java.io.PrintStream;
import java.util.concurrent.Executors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import com.amitinside.aspecio.examples.DemoConsumer;
import com.amitinside.aspecio.examples.async.SuperSlowService;
import com.amitinside.aspecio.examples.greetings.GoodBye;
import com.amitinside.aspecio.examples.greetings.Hello;

@Component
public final class DemoConsumerImpl implements DemoConsumer {

	@Reference
	private Hello hello;

	@Reference
	private GoodBye goodbye;

	@Reference
	private SuperSlowService superSlowService;

	private final PromiseFactory promiseFactory;

	@Activate
	public DemoConsumerImpl() {
		promiseFactory = new PromiseFactory(Executors.newSingleThreadExecutor());
	}

	@Override
	public void consumeTo(final PrintStream out) {
		out.println(hello.hello() + " " + goodbye.goodbye());
	}

	@Override
	public Promise<Long> getLongResult() {
		final Deferred<Long> d = promiseFactory.deferred();
		final Promise<Long> promise = superSlowService.compute();
		promise.onResolve(() -> new Thread(() -> d.resolveWith(promise)).start());
		return d.getPromise();
	}

}
