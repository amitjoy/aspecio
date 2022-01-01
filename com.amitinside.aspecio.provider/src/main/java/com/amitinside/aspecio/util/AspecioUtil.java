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
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw Exceptions.duck(e);
        }
    }

    public static String asString(final Object propObj) {
        String res;
        if (propObj == null) {
            res = null;
        } else if (propObj instanceof String[] && ((String[]) propObj).length == 1) {
            res = ((String[]) propObj)[0];
        } else if (propObj instanceof String) {
            res = (String) propObj;
        } else {
            throw new IllegalArgumentException("Can only convert properties of type String or String[] of size 1");
        }
        return res;
    }

    public static String[] asStringArray(final Object propObj) {
        String[] res = null;
        if (propObj == null) {
            res = new String[0];
        } else if (propObj instanceof String[]) {
            res = (String[]) propObj;
        } else if (propObj instanceof String) {
            res = new String[] { (String) propObj };
        } else {
            throw new IllegalArgumentException("Can only convert properties of type String or String[]");
        }
        return res;
    }

    public static long asLong(final Object propObj) {
        if (propObj instanceof Number) {
            return ((Number) propObj).longValue();
        } else {
            throw new IllegalArgumentException("Can only convert properties of type Number");
        }
    }

    public static int asInt(final Object propObj, final int defaultValue) {
        return propObj instanceof Integer ? (Integer) propObj : defaultValue;
    }

    public static <T> T firstOrNull(final SortedSet<T> set) {
        return set == null || set.isEmpty() ? null : set.first();
    }

    public static <T> Set<T> asSet(final Collection<T> source) {
        return source.stream().collect(toSet());
    }

    public static <T> Set<String> asSet(final T source) {
        return Stream.of(asStringArray(source)).collect(toSet());
    }

    public static <T> List<String> asList(final T source) {
        return Stream.of(asStringArray(source)).collect(toList());
    }

}