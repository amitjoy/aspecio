package com.amitinside.aspecio.logging;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.osgi.service.log.LogLevel.AUDIT;
import static org.osgi.service.log.LogLevel.ERROR;
import static org.osgi.service.log.LogLevel.INFO;
import static org.osgi.service.log.LogLevel.WARN;

import java.io.PrintStream;
import java.time.LocalDateTime;

import org.osgi.service.log.LogLevel;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerConsumer;
import org.osgi.service.log.LoggerFactory;

/**
 * A placeholder logger to be used in case when OSGi {@link LoggerFactory} is not available. This
 * logger instance prints out the message on the system output stream.
 */
class NullLogger implements Logger {

    @Override
    public String getName() {
        return "NULL_LOGGER";
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(final String message) {
        log(LogLevel.TRACE, message);
    }

    @Override
    public void trace(final String format, final Object arg) {
        log(LogLevel.TRACE, format, arg);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        log(LogLevel.TRACE, format, arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... arguments) {
        log(LogLevel.TRACE, format, arguments);
    }

    @Override
    public <E extends Exception> void trace(final LoggerConsumer<E> consumer) throws E {
        // no-op
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(final String message) {
        log(LogLevel.DEBUG, message);
    }

    @Override
    public void debug(final String format, final Object arg) {
        log(LogLevel.DEBUG, format, arg);
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        log(LogLevel.DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... arguments) {
        log(LogLevel.DEBUG, format, arguments);
    }

    @Override
    public <E extends Exception> void debug(final LoggerConsumer<E> consumer) throws E {
        // no-op
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(final String message) {
        log(INFO, message);
    }

    @Override
    public void info(final String format, final Object arg) {
        log(INFO, format, arg);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        log(INFO, format, arg1, arg2);
    }

    @Override
    public void info(final String format, final Object... arguments) {
        log(INFO, format, arguments);
    }

    @Override
    public <E extends Exception> void info(final LoggerConsumer<E> consumer) throws E {
        // no-op
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(final String message) {
        log(WARN, message);
    }

    @Override
    public void warn(final String format, final Object arg) {
        log(WARN, format, arg);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        log(WARN, format, arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... arguments) {
        log(WARN, format, arguments);
    }

    @Override
    public <E extends Exception> void warn(final LoggerConsumer<E> consumer) throws E {
        // no-op
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(final String message) {
        log(ERROR, message);
    }

    @Override
    public void error(final String format, final Object arg) {
        log(ERROR, format, arg);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        log(ERROR, format, arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... arguments) {
        log(ERROR, format, arguments);
    }

    @Override
    public <E extends Exception> void error(final LoggerConsumer<E> consumer) throws E {
        // no-op
    }

    @Override
    public void audit(final String message) {
        log(AUDIT, message);
    }

    @Override
    public void audit(final String format, final Object arg) {
        log(AUDIT, format, arg);
    }

    @Override
    public void audit(final String format, final Object arg1, final Object arg2) {
        log(AUDIT, format, arg1, arg2);
    }

    @Override
    public void audit(final String format, final Object... arguments) {
        log(AUDIT, format, arguments);
    }

    private void log(final LogLevel level, final String format, final Object... arguments) {
        PrintStream stream = null;
        if (level.ordinal() <= ERROR.ordinal()) {
            stream = System.err; // NOSONAR
        } else {
            stream = System.out; // NOSONAR
        }
        final String datetime     = LocalDateTime.now().format(ISO_LOCAL_DATE_TIME);
        final String formattedLog = format.replaceAll("\\{\\}", "%s");
        stream.println("[" + datetime + "][" + level + "] " + String.format(formattedLog, arguments));
        if (arguments != null && arguments.length != 0 && arguments[arguments.length - 1] instanceof Throwable) {
            ((Throwable) arguments[arguments.length - 1]).printStackTrace(); // NOSONAR
        }
    }
}
