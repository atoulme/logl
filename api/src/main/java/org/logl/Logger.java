package org.logl;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A logl {@code Logger}.
 *
 * <p>
 * This interface provides all the common logging methods used at runtime.
 */
public interface Logger {

  /**
   * @return A {@link Logger} that discards all log messages.
   */
  static Logger nullProvider() {
    return NullLogger.instance();
  }

  /**
   * @return The {@link Level} that logging is set to.
   */
  Level getLevel();

  /**
   * Check if a logging level is enabled.
   *
   * @param level The {@link Level} to check.
   * @return {@code true} if logging is enabled for that level.
   */
  boolean isEnabled(Level level);

  /**
   * Get a {@link LogWriter} for the specified level.
   *
   * @param level The {@link Level} the writer should log to.
   * @return A {@link LogWriter} that writes logs at the specified level.
   */
  default LogWriter writer(Level level) {
    switch (level) {
      case ERROR:
        return errorWriter();
      case WARN:
        return warnWriter();
      case INFO:
        return infoWriter();
      case DEBUG:
        return debugWriter();
      case NONE:
        // fall through
    }
    return NullLogWriter.instance();
  }

  /**
   * @return {@code true} if the log level {@link Level#ERROR} is enabled.
   */
  default boolean isErrorEnabled() {
    return isEnabled(Level.ERROR);
  }

  /**
   * Log a {@link LogMessage} at level {@code ERROR}, if enabled.
   *
   * @param message The message to log.
   */
  default void error(LogMessage message) {
    errorWriter().log(message);
  }

  /**
   * Log a message at level {@code ERROR}, if enabled.
   *
   * @param message A character sequence (or {@link String}).
   */
  default void error(CharSequence message) {
    errorWriter().log(message);
  }

  /**
   * Log the supplied message at level {@code ERROR}, if enabled.
   *
   * The supplier will only be invoked if the level {@code ERROR} is enabled. Use this method to avoid expensive string
   * construction when unnecessary.
   *
   * @param messageSupplier A {@link Supplier} for the message.
   */
  default void error(Supplier<? extends CharSequence> messageSupplier) {
    errorWriter().log(messageSupplier);
  }

  /**
   * Log a message and exception at level {@code ERROR}, if enabled.
   *
   * @param message The message to log.
   * @param cause The exception to log.
   */
  default void error(LogMessage message, Throwable cause) {
    errorWriter().log(message, cause);
  }

  /**
   * Log a message and exception at level {@code ERROR}, if enabled.
   *
   * @param message A character sequence (or {@link String}).
   * @param cause The exception to log.
   */
  default void error(CharSequence message, Throwable cause) {
    errorWriter().log(message, cause);
  }

  /**
   * Log the supplied message and exception at level {@code ERROR}, if enabled.
   *
   * The supplier will only be invoked if the level {@code ERROR} is enabled. Use this method to avoid expensive string
   * construction when unnecessary.
   *
   * @param messageSupplier A {@link Supplier} for the message.
   * @param cause The exception to log.
   */
  default void error(Supplier<? extends CharSequence> messageSupplier, Throwable cause) {
    errorWriter().log(messageSupplier, cause);
  }

  /**
   * Log a pattern formatted message at level {@code ERROR}, if enabled.
   *
   * This is equivalent to {@code error(LogMessage.patternFormat(pattern, args))}, but may be slightly more efficient in
   * some logging implementations.
   *
   * @param pattern The format string, as per {@link LogMessage#patternFormat(String, Object...)}.
   * @param args The format arguments.
   */
  default void error(String pattern, Object... args) {
    errorWriter().log(pattern, args);
  }

  /**
   * Log a string formatted message at level {@code ERROR}, if enabled.
   *
   * This is equivalent to {@code info(LogMessage.stringFormat(format, args))}, but may be slightly more efficient in
   * some logging implementations.
   *
   * @param format The format pattern, as per {@link LogMessage#stringFormat(String, Object...)}.
   * @param args The format arguments.
   */
  default void errorf(String format, Object... args) {
    errorWriter().logf(format, args);
  }

  /**
   * @return A {@link LogWriter} that writes logs at level {@code ERROR}.
   */
  LogWriter errorWriter();

  /**
   * @return {@code true} if the log level {@link Level#WARN} is enabled.
   */
  default boolean isWarnEnabled() {
    return isEnabled(Level.WARN);
  }

  /**
   * Log a {@link LogMessage} at level {@code WARN}, if enabled.
   *
   * @param message The message to log.
   */
  default void warn(LogMessage message) {
    warnWriter().log(message);
  }

  /**
   * Log a message at level {@code WARN}, if enabled.
   *
   * @param message A character sequence (or {@link String}).
   */
  default void warn(CharSequence message) {
    warnWriter().log(message);
  }

  /**
   * Log the supplied message at level {@code WARN}, if enabled.
   *
   * The supplier will only be invoked if the level {@code WARN} is enabled. Use this method to avoid expensive string
   * construction when unnecessary.
   *
   * @param messageSupplier A {@link Supplier} for the message.
   */
  default void warn(Supplier<? extends CharSequence> messageSupplier) {
    warnWriter().log(messageSupplier);
  }

  /**
   * Log a message and exception at level {@code WARN}, if enabled.
   *
   * @param message The message to log.
   * @param cause The exception to log.
   */
  default void warn(LogMessage message, Throwable cause) {
    warnWriter().log(message, cause);
  }

  /**
   * Log a message and exception at level {@code WARN}, if enabled.
   *
   * @param message A character sequence (or {@link String}).
   * @param cause The exception to log.
   */
  default void warn(CharSequence message, Throwable cause) {
    warnWriter().log(message, cause);
  }

  /**
   * Log the supplied message and exception at level {@code WARN}, if enabled.
   *
   * The supplier will only be invoked if the level {@code WARN} is enabled. Use this method to avoid expensive string
   * construction when unnecessary.
   *
   * @param messageSupplier A {@link Supplier} for the message.
   * @param cause The exception to log.
   */
  default void warn(Supplier<? extends CharSequence> messageSupplier, Throwable cause) {
    warnWriter().log(messageSupplier, cause);
  }

  /**
   * Log a pattern formatted message at level {@code WARN}, if enabled.
   *
   * This is equivalent to {@code warn(LogMessage.patternFormat(pattern, args))}, but may be slightly more efficient in
   * some logging implementations.
   *
   * @param pattern The format string, as per {@link LogMessage#patternFormat(String, Object...)}.
   * @param args The format arguments.
   */
  default void warn(String pattern, Object... args) {
    warnWriter().log(pattern, args);
  }

  /**
   * Log a string formatted message at level {@code WARN}, if enabled.
   *
   * This is equivalent to {@code info(LogMessage.stringFormat(format, args))}, but may be slightly more efficient in
   * some logging implementations.
   *
   * @param format The format pattern, as per {@link LogMessage#stringFormat(String, Object...)}.
   * @param args The format arguments.
   */
  default void warnf(String format, Object... args) {
    warnWriter().logf(format, args);
  }

  /**
   * @return A {@link LogWriter} that writes logs at level {@code WARN}.
   */
  LogWriter warnWriter();

  /**
   * @return {@code true} if the log level {@link Level#INFO} is enabled.
   */
  default boolean isInfoEnabled() {
    return isEnabled(Level.INFO);
  }

  /**
   * Log a {@link LogMessage} at level {@code INFO}, if enabled.
   *
   * @param message The message to log.
   */
  default void info(LogMessage message) {
    infoWriter().log(message);
  }

  /**
   * Log a message at level {@code INFO}, if enabled.
   *
   * @param message A character sequence (or {@link String}).
   */
  default void info(CharSequence message) {
    infoWriter().log(message);
  }

  /**
   * Log the supplied message at level {@code INFO}, if enabled.
   *
   * The supplier will only be invoked if the level {@code INFO} is enabled. Use this method to avoid expensive string
   * construction when unnecessary.
   *
   * @param messageSupplier A {@link Supplier} for the message.
   */
  default void info(Supplier<? extends CharSequence> messageSupplier) {
    infoWriter().log(messageSupplier);
  }

  /**
   * Log a message and exception at level {@code INFO}, if enabled.
   *
   * @param message The message to log.
   * @param cause The exception to log.
   */
  default void info(CharSequence message, Throwable cause) {
    infoWriter().log(message, cause);
  }

  /**
   * Log a message and exception at level {@code INFO}, if enabled.
   *
   * @param message A character sequence (or {@link String}).
   * @param cause The exception to log.
   */
  default void info(LogMessage message, Throwable cause) {
    infoWriter().log(message, cause);
  }

  /**
   * Log the supplied message and exception at level {@code INFO}, if enabled.
   *
   * The supplier will only be invoked if the level {@code INFO} is enabled. Use this method to avoid expensive string
   * construction when unnecessary.
   *
   * @param messageSupplier A {@link Supplier} for the message.
   * @param cause The exception to log.
   */
  default void info(Supplier<? extends CharSequence> messageSupplier, Throwable cause) {
    infoWriter().log(messageSupplier, cause);
  }

  /**
   * Log a pattern formatted message at level {@code INFO}, if enabled.
   *
   * This is equivalent to {@code info(LogMessage.patternFormat(pattern, args))}, but may be slightly more efficient in
   * some logging implementations.
   *
   * @param pattern The format pattern, as per {@link LogMessage#patternFormat(String, Object...)}.
   * @param args The format arguments.
   */
  default void info(String pattern, Object... args) {
    infoWriter().log(pattern, args);
  }

  /**
   * Log a string formatted message at level {@code INFO}, if enabled.
   *
   * This is equivalent to {@code info(LogMessage.stringFormat(format, args))}, but may be slightly more efficient in
   * some logging implementations.
   *
   * @param format The format pattern, as per {@link LogMessage#stringFormat(String, Object...)}.
   * @param args The format arguments.
   */
  default void infof(String format, Object... args) {
    infoWriter().logf(format, args);
  }

  /**
   * @return A {@link LogWriter} that writes logs at level {@code INFO}.
   */
  LogWriter infoWriter();

  /**
   * @return {@code true} if the log level {@link Level#DEBUG} is enabled.
   */
  default boolean isDebugEnabled() {
    return isEnabled(Level.DEBUG);
  }

  /**
   * Log a {@link LogMessage} at level {@code DEBUG}, if enabled.
   *
   * @param message The message to log.
   */
  default void debug(LogMessage message) {
    debugWriter().log(message);
  }

  /**
   * Log a message at level {@code DEBUG}, if enabled.
   *
   * @param message A character sequence (or {@link String}).
   */
  default void debug(CharSequence message) {
    debugWriter().log(message);
  }

  /**
   * Log the supplied message at level {@code DEBUG}, if enabled.
   *
   * The supplier will only be invoked if the level {@code DEBUG} is enabled. Use this method to avoid expensive string
   * construction when unnecessary.
   *
   * @param messageSupplier A {@link Supplier} for the message.
   */
  default void debug(Supplier<? extends CharSequence> messageSupplier) {
    debugWriter().log(messageSupplier);
  }

  /**
   * Log a message and exception at level {@code DEBUG}, if enabled.
   *
   * @param message The message to log.
   * @param cause The exception to log.
   */
  default void debug(LogMessage message, Throwable cause) {
    debugWriter().log(message, cause);
  }

  /**
   * Log a message and exception at level {@code DEBUG}, if enabled.
   *
   * @param message A character sequence (or {@link String}).
   * @param cause The exception to log.
   */
  default void debug(CharSequence message, Throwable cause) {
    debugWriter().log(message, cause);
  }

  /**
   * Log the supplied message and exception at level {@code DEBUG}, if enabled.
   *
   * The supplier will only be invoked if the level {@code DEBUG} is enabled. Use this method to avoid expensive string
   * construction when unnecessary.
   *
   * @param messageSupplier A {@link Supplier} for the message.
   * @param cause The exception to log.
   */
  default void debug(Supplier<? extends CharSequence> messageSupplier, Throwable cause) {
    debugWriter().log(messageSupplier, cause);
  }

  /**
   * Log a pattern formatted message at level {@code DEBUG}, if enabled.
   *
   * This is equivalent to {@code debug(LogMessage.patternFormat(pattern, args))}, but may be slightly more efficient in
   * some logging implementations.
   *
   * @param pattern The format string, as per {@link LogMessage#patternFormat(String, Object...)}.
   * @param args The format arguments.
   */
  default void debug(String pattern, Object... args) {
    debugWriter().log(pattern, args);
  }

  /**
   * Log a string formatted message at level {@code DEBUG}, if enabled.
   *
   * This is equivalent to {@code debug(LogMessage.stringFormat(format, args))}, but may be slightly more efficient in
   * some logging implementations.
   *
   * @param format The format string, as per {@link LogMessage#stringFormat(String, Object...)}.
   * @param args The format arguments.
   */
  default void debugf(String format, Object... args) {
    debugWriter().logf(format, args);
  }

  /**
   * @return A {@link LogWriter} that writes logs at level {@code DEBUG}.
   */
  LogWriter debugWriter();

  /**
   * Write a set of log messages in a batch.
   *
   * <p>
   * The specified {@link Consumer} will be invoked with a {@link Logger} that collects all log output and then writes
   * them all consecutively when the consumer invocation returns. Use this when a set of log lines must appear
   * consecutively in the log, regardless of time ordering.
   * <p>
   * Note: most implementations do not guarantee log ordering, so log lines may not always be ordered correctly by
   * timestamp. This is especially true when logs are written in batch.
   *
   * @param fn A {@link Consumer} that will be invoked with the batching {@link Logger}.
   */
  void batch(Consumer<Logger> fn);
}
