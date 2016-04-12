package io.lambdacube.aspecio.internal;

import java.util.SortedSet;
import java.util.concurrent.Callable;

public final class AspecioUtils {

    private AspecioUtils() {
    }

    // Run any block of code and propagate throwables as necessary
    public static <T> T trust(Callable<T> block) {
        try {
            return block.call();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] asStringProperties(Object propObj) {
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

    public static int getIntValue(Object propObj, int defaultValue) {
        if (propObj instanceof Integer) {
            return ((Integer) propObj).intValue();
        } else {
            return defaultValue;
        }
    }

    public static <T> T firstOrNull(SortedSet<T> set) {
        if (set.isEmpty()) {
            return null;
        }
        return set.first();
    }
}
