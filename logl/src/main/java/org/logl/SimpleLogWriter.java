package org.logl;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class SimpleLogWriter implements LogWriter {
  private final Level level;
  private final SimpleLoggerImpl logger;
  private final Supplier<Instant> currentTimeSupplier;

  SimpleLogWriter(Level level, SimpleLoggerImpl logger, Supplier<Instant> currentTimeSupplier) {
    this.level = level;
    this.logger = logger;
    this.currentTimeSupplier = currentTimeSupplier;
  }

  @Override
  public void log(LogMessage message) {
    requireNonNull(message);
    logger.log(level, message);
  }

  @Override
  public void log(CharSequence message) {
    requireNonNull(message);
    logger.log(level, message);
  }

  @Override
  public void log(LogMessage message, Throwable cause) {
    requireNonNull(message);
    logger.log(level, message, cause);
  }

  @Override
  public void log(CharSequence message, Throwable cause) {
    requireNonNull(message);
    logger.log(level, message, cause);
  }

  @Override
  public void logf(String format, Object... args) {
    requireNonNull(format);
    logger.logf(level, format, args);
  }

  @Override
  public void batch(Consumer<LogWriter> fn) {
    requireNonNull(fn);
    List<SimpleLogEvent> events = new ArrayList<>(32);
    fn.accept(new BatchLogWriter(level, currentTimeSupplier, events::add));
    logger.writeEvents(events);
  }

  static final class BatchLogWriter implements LogWriter {
    private final Level level;
    private final Supplier<Instant> currentTimeSupplier;
    private final Consumer<SimpleLogEvent> eventConsumer;

    BatchLogWriter(Level level, Supplier<Instant> currentTimeSupplier, Consumer<SimpleLogEvent> eventConsumer) {
      this.level = level;
      this.currentTimeSupplier = currentTimeSupplier;
      this.eventConsumer = eventConsumer;
    }

    @Override
    public void log(LogMessage message) {
      requireNonNull(message);
      eventConsumer.accept(new SimpleLogEvent(currentTimeSupplier.get(), level, message, null));
    }

    @Override
    public void log(CharSequence message) {
      requireNonNull(message);
      eventConsumer.accept(new SimpleLogEvent(currentTimeSupplier.get(), level, message, null));
    }

    @Override
    public void log(LogMessage message, Throwable cause) {
      requireNonNull(message);
      eventConsumer.accept(new SimpleLogEvent(currentTimeSupplier.get(), level, message, cause));
    }

    @Override
    public void log(CharSequence message, Throwable cause) {
      requireNonNull(message);
      eventConsumer.accept(new SimpleLogEvent(currentTimeSupplier.get(), level, message, cause));
    }

    @Override
    public void batch(Consumer<LogWriter> fn) {
      requireNonNull(fn);
      fn.accept(this);
    }
  }
}
