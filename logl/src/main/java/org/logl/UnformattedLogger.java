package org.logl;

import static java.util.Objects.requireNonNull;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Factory methods for creating loggers that writes log lines without any formatting or adornment.
 */
public final class UnformattedLogger {
  private UnformattedLogger() {}

  /**
   * Start building an unformatted logger that uses the specified locale for message output.
   *
   * @param locale The locale to use for message output.
   * @return A builder for an unformatted logger.
   */
  public static Builder withLocale(Locale locale) {
    return new Builder().withLocale(locale);
  }

  /**
   * Start building an unformatted logger that writes log lines at or above the specified level.
   *
   * @param level The level at or above which log lines will be output.
   * @return A builder for an unformatted logger.
   */
  public static Builder withLogLevel(Level level) {
    return new Builder().withLogLevel(level);
  }

  /**
   * Start building an unformatted logger that does not flush the output after each write.
   *
   * @return A builder for an unformatted logger.
   */
  public static Builder withoutAutoFlush() {
    return new Builder().withoutAutoFlush();
  }

  /**
   * Create a simple {@link AdjustableLoggerProvider} instance that writes logs to the supplied {@link PrintWriter}.
   *
   * @param writer A {@link PrintWriter} to output log lines to.
   * @return A {@link AdjustableLoggerProvider} that provides {@link Logger} instances that do unformatted logging.
   */
  public static AdjustableLoggerProvider toPrintWriter(PrintWriter writer) {
    return new Builder().toPrintWriter(writer);
  }

  /**
   * Create a simple {@link AdjustableLoggerProvider} instance that writes logs to the supplied {@link PrintWriter}.
   *
   * @param writerSupplier A {@link Supplier} for a {@link PrintWriter}, where log lines will be output to.
   * @return A {@link AdjustableLoggerProvider} that provides {@link Logger} instances that do unformatted logging.
   */
  public static AdjustableLoggerProvider toPrintWriter(Supplier<PrintWriter> writerSupplier) {
    return new Builder().toPrintWriter(writerSupplier);
  }

  /**
   * A builder for a logger that does unformatted logging.
   */
  public static class Builder {
    Locale locale = Locale.getDefault();
    Level level = Level.INFO;
    boolean autoFlush = true;

    /**
     * Use the specified locale for message output.
     *
     * @param locale The locale to use for message output.
     * @return This builder.
     */
    public Builder withLocale(Locale locale) {
      requireNonNull(locale);
      this.locale = locale;
      return this;
    }

    /**
     * Write log lines at or above the specified level.
     *
     * @param level The level at or above which log lines will be output.
     * @return This builder.
     */
    public Builder withLogLevel(Level level) {
      requireNonNull(level);
      this.level = level;
      return this;
    }

    /**
     * Do not flush the output after each write.
     *
     * @return This builder.
     */
    public Builder withoutAutoFlush() {
      this.autoFlush = false;
      return this;
    }

    /**
     * Create a simple {@link AdjustableLoggerProvider} instance that writes logs to the supplied {@link PrintWriter}.
     *
     * @param writer A {@link PrintWriter} to output log lines to.
     * @return A {@link AdjustableLoggerProvider} that provides {@link Logger} instances that do unformatted logging.
     */
    public AdjustableLoggerProvider toPrintWriter(PrintWriter writer) {
      requireNonNull(writer);
      return toPrintWriter(() -> writer);
    }

    /**
     * Create a simple {@link AdjustableLoggerProvider} instance that writes logs to the supplied {@link PrintWriter}.
     *
     * @param writerSupplier A {@link Supplier} for a {@link PrintWriter}, where log lines will be output to.
     * @return A {@link AdjustableLoggerProvider} that provides {@link Logger} instances that do unformatted logging.
     */
    public AdjustableLoggerProvider toPrintWriter(Supplier<PrintWriter> writerSupplier) {
      requireNonNull(writerSupplier);
      return new Provider(this, writerSupplier);
    }
  }

  private static class Provider implements AdjustableLoggerProvider {
    private final Builder builder;
    private final Supplier<PrintWriter> writerSupplier;
    private final ConcurrentHashMap<String, UnformattedLoggerImpl> loggers = new ConcurrentHashMap<>();

    private Provider(Builder builder, Supplier<PrintWriter> writerSupplier) {
      this.builder = builder;
      this.writerSupplier = writerSupplier;
    }

    @Override
    public AdjustableLogger getLogger(String name) {
      return loggers.computeIfAbsent(name, n -> new UnformattedLoggerImpl(builder, writerSupplier));
    }
  }
}
