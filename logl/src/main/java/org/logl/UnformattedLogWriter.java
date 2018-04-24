package org.logl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class UnformattedLogWriter implements LogWriter {
  private final Level level;
  private final UnformattedLoggerImpl logger;
  private final Locale locale;
  private final Supplier<PrintWriter> writerSupplier;
  private final boolean autoFlush;

  UnformattedLogWriter(
      Level level,
      UnformattedLoggerImpl logger,
      Locale locale,
      Supplier<PrintWriter> writerSupplier,
      boolean autoFlush) {
    this.level = level;
    this.logger = logger;
    this.locale = locale;
    this.writerSupplier = writerSupplier;
    this.autoFlush = autoFlush;
  }

  @Override
  public void log(LogMessage message) {
    requireNonNull(message);
    if (!logger.isEnabled(level)) {
      return;
    }
    PrintWriter out;
    synchronized (logger) {
      out = writerSupplier.get();
      try {
        message.appendTo(locale, out);
      } catch (IOException ex) {
        // PrintWriter does not throw this exception
        throw new RuntimeException("unexpected exception", ex);
      }
      out.println();
    }
    if (autoFlush) {
      out.flush();
    }
  }

  @Override
  public void log(CharSequence message) {
    requireNonNull(message);
    if (!logger.isEnabled(level)) {
      return;
    }
    PrintWriter out;
    synchronized (logger) {
      out = writerSupplier.get();
      out.println(message);
    }
    if (autoFlush) {
      out.flush();
    }
  }

  @Override
  public void log(LogMessage message, Throwable cause) {
    if (cause == null) {
      log(message);
      return;
    }
    requireNonNull(message);
    if (!logger.isEnabled(level)) {
      return;
    }
    PrintWriter out;
    synchronized (logger) {
      out = writerSupplier.get();
      try {
        message.appendTo(locale, out);
      } catch (IOException ex) {
        // PrintWriter does not throw this exception
        throw new RuntimeException("unexpected exception", ex);
      }
      out.println();
      cause.printStackTrace(out);
    }
    if (autoFlush) {
      out.flush();
    }
  }

  @Override
  public void log(CharSequence message, Throwable cause) {
    if (cause == null) {
      log(message);
      return;
    }
    requireNonNull(message);
    if (!logger.isEnabled(level)) {
      return;
    }
    PrintWriter out;
    synchronized (logger) {
      out = writerSupplier.get();
      out.println(message);
      cause.printStackTrace(out);
    }
    if (autoFlush) {
      out.flush();
    }
  }

  @Override
  public void logf(String format, Object... args) {
    requireNonNull(format);
    if (!logger.isEnabled(level)) {
      return;
    }
    PrintWriter out;
    synchronized (logger) {
      out = writerSupplier.get();
      out.printf(format, args);
      out.println();
    }
    if (autoFlush) {
      out.flush();
    }
  }

  @Override
  public void batch(Consumer<LogWriter> fn) {
    requireNonNull(fn);
    List<UnformattedLogEvent> events = new ArrayList<>(32);
    fn.accept(new BatchLogWriter(level, events::add));
    logger.writeEvents(events);
  }

  static final class BatchLogWriter implements LogWriter {
    private final Level level;
    private final Consumer<UnformattedLogEvent> eventConsumer;

    BatchLogWriter(Level level, Consumer<UnformattedLogEvent> eventConsumer) {
      this.level = level;
      this.eventConsumer = eventConsumer;
    }

    @Override
    public void log(LogMessage message) {
      requireNonNull(message);
      eventConsumer.accept(new UnformattedLogEvent(level, message, null));
    }

    @Override
    public void log(CharSequence message) {
      requireNonNull(message);
      eventConsumer.accept(new UnformattedLogEvent(level, message, null));
    }

    @Override
    public void log(LogMessage message, Throwable cause) {
      requireNonNull(message);
      eventConsumer.accept(new UnformattedLogEvent(level, message, cause));
    }

    @Override
    public void log(CharSequence message, Throwable cause) {
      requireNonNull(message);
      eventConsumer.accept(new UnformattedLogEvent(level, message, cause));
    }

    @Override
    public void batch(Consumer<LogWriter> fn) {
      fn.accept(this);
    }
  }
}
