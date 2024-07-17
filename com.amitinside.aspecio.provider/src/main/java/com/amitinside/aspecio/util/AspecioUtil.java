/*******************************************************************************
 * Copyright 2022-2024 Amit Kumar Mondal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
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
package com.amitinside.aspecio.util;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public final class AspecioUtil {

	private AspecioUtil() {
		throw new IllegalAccessError("Cannot be instantiated");
	}

	// Run any block of code and propagate throwables as necessary
	public static <T> T trust(final Callable<T> block) {
		try {
			return block.call();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw Exceptions.duck(e);
		}
	}

	public static String asString(final Object propObj) {
		if (propObj instanceof String) {
			return (String) propObj;
		} else if (propObj instanceof String[] && ((String[]) propObj).length == 1) {
			return ((String[]) propObj)[0];
		} else if (propObj == null) {
			return null;
		}
		throw new IllegalArgumentException("Can only convert properties of type String or String[] of size 1");
	}

	public static String[] asStringArray(final Object propObj) {
		if (propObj instanceof String) {
			return new String[] { (String) propObj };
		} else if (propObj instanceof String[]) {
			return (String[]) propObj;
		} else if (propObj == null) {
			return new String[0];
		}
		throw new IllegalArgumentException("Can only convert properties of type String or String[]");
	}

	public static long asLong(final Object propObj) {
		if (propObj instanceof Number) {
			return ((Number) propObj).longValue();
		}
		throw new IllegalArgumentException("Can only convert properties of type Number");
	}

	public static int asInt(final Object propObj, final int defaultValue) {
		return (propObj instanceof Integer) ? (Integer) propObj : defaultValue;
	}

	public static <T> T firstOrNull(final SortedSet<T> set) {
		return (set == null || set.isEmpty()) ? null : set.first();
	}

	public static <T> Set<T> asSet(final Collection<T> source) {
		return source.stream().collect(toSet());
	}

	public static Set<String> asSet(final Object source) {
		return Stream.of(asStringArray(source)).collect(toSet());
	}

	public static List<String> asList(final Object source) {
		return Stream.of(asStringArray(source)).collect(toList());
	}
}