/*******************************************************************************
 * Copyright 2022-2023 Amit Kumar Mondal
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
package com.amitinside.aspecio.service;

import java.util.Set;

import io.primeval.reflex.proxy.Interceptor;

public final class AspectInterceptorContext {

	public final Interceptor interceptor;
	public final Set<String> satisfiedAspects;
	public final Set<String> satisfiedRequiredAspects;
	public final Set<String> unsatisfiedRequiredAspects;
	public final Set<String> satisfiedOptionalAspects;
	public final Set<String> unsatisfiedOptionalAspects;
	public final Set<String> extraProperties;

	public AspectInterceptorContext(final Interceptor interceptor, final Set<String> satisfiedAspects,
			final Set<String> satisfiedRequiredAspects, final Set<String> unsatisfiedRequiredAspects,
			final Set<String> satisfiedOptionalAspects, final Set<String> unsatisfiedOptionalAspects,
			final Set<String> extraProperties) {
		this.interceptor = interceptor;
		this.satisfiedAspects = satisfiedAspects;
		this.satisfiedRequiredAspects = satisfiedRequiredAspects;
		this.unsatisfiedRequiredAspects = unsatisfiedRequiredAspects;
		this.satisfiedOptionalAspects = satisfiedOptionalAspects;
		this.unsatisfiedOptionalAspects = unsatisfiedOptionalAspects;
		this.extraProperties = extraProperties;
	}

}
