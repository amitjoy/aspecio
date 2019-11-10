package com.amitinside.aspecio.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.log.Logger;

import com.amitinside.aspecio.logging.AspecioLogger;

public final class AspecioUtil {

    private static final Logger logger = AspecioLogger.getLogger(AspecioUtil.class);

    private AspecioUtil() {
        throw new IllegalAccessError("Cannot be instantiated");
    }

    // Run any block of code and propagate throwables as necessary
    public static <T> T trust(final Callable<T> block) {
        try {
            return block.call();
        } catch (RuntimeException | Error e) {
            logger.error("Error while running code", e);
            throw e;
        } catch (final Exception e) {
            logger.error("Exception while running code", e);
            throw Exceptions.duck(e);
        }
    }

    public static String asStringProperty(final Object propObj) {
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

    public static String[] asStringProperties(final Object propObj) {
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

    public static long getLongValue(final Object propObj) {
        if (propObj instanceof Number) {
            return ((Number) propObj).longValue();
        } else {
            throw new IllegalArgumentException("Can only convert properties of type Number");
        }
    }

    public static int getIntValue(final Object propObj, final int defaultValue) {
        return propObj instanceof Integer ? (Integer) propObj : defaultValue;
    }

    public static <T> T firstOrNull(final SortedSet<T> set) {
        return set == null || set.isEmpty() ? null : set.first();
    }

    public static <T> Set<T> copySet(final Collection<T> source) {
        final Set<T> copy = new LinkedHashSet<>(source.size());
        copy.addAll(source);
        return copy;
    }

    public static void registerGogoCommand(final Object gogoCommand) {
        final String[]                   commandNames  = Stream.of(gogoCommand.getClass().getMethods())
                .map(Method::getName).toArray(String[]::new);
        final BundleContext              bundleContext = FrameworkUtil.getBundle(AspecioUtil.class).getBundleContext();
        final Dictionary<String, Object> props         = new Hashtable<>();

        props.put("osgi.command.scope", "aspecio");
        props.put("osgi.command.function", commandNames);

        bundleContext.registerService(Object.class, gogoCommand, props);
    }

}