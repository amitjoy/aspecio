package com.amitinside.aspecio.logging;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    log(LogLevel.INFO, message);
  }

  @Override
  public void info(final String format, final Object arg) {
    log(LogLevel.INFO, format, arg);
  }

  @Override
  public void info(final String format, final Object arg1, final Object arg2) {
    log(LogLevel.INFO, format, arg1, arg2);
  }

  @Override
  public void info(final String format, final Object... arguments) {
    log(LogLevel.INFO, format, arguments);
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
    log(LogLevel.WARN, message);
  }

  @Override
  public void warn(final String format, final Object arg) {
    log(LogLevel.WARN, format, arg);
  }

  @Override
  public void warn(final String format, final Object arg1, final Object arg2) {
    log(LogLevel.WARN, format, arg1, arg2);
  }

  @Override
  public void warn(final String format, final Object... arguments) {
    log(LogLevel.WARN, format, arguments);
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
    log(LogLevel.ERROR, message);
  }

  @Override
  public void error(final String format, final Object arg) {
    log(LogLevel.ERROR, format, arg);
  }

  @Override
  public void error(final String format, final Object arg1, final Object arg2) {
    log(LogLevel.ERROR, format, arg1, arg2);
  }

  @Override
  public void error(final String format, final Object... arguments) {
    log(LogLevel.ERROR, format, arguments);
  }

  @Override
  public <E extends Exception> void error(final LoggerConsumer<E> consumer) throws E {
    // no-op
  }

  @Override
  public void audit(final String message) {
    log(LogLevel.AUDIT, message);
  }

  @Override
  public void audit(final String format, final Object arg) {
    log(LogLevel.AUDIT, format, arg);
  }

  @Override
  public void audit(final String format, final Object arg1, final Object arg2) {
    log(LogLevel.AUDIT, format, arg1, arg2);
  }

  @Override
  public void audit(final String format, final Object... arguments) {
    log(LogLevel.AUDIT, format, arguments);
  }

  private void log(final LogLevel level, final String format, final Object... arguments) {
    PrintStream stream = null;
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      stream = System.err; // NOSONAR
    } else {
      stream = System.out; // NOSONAR
    }
    final String datetime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    stream.println("[" + datetime + "][" + level + "] " + String.format(format, arguments));
    if (arguments != null && arguments.length != 0
        && arguments[arguments.length - 1] instanceof Throwable) {
      ((Throwable) arguments[arguments.length - 1]).printStackTrace(); // NOSONAR
    }
  }
}
