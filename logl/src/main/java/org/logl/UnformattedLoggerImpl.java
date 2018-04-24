package org.logl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.logl.UnformattedLogWriter.BatchLogWriter;
import org.logl.UnformattedLogger.Builder;

final class UnformattedLoggerImpl implements AdjustableLogger {

  private final Locale locale;
  private final AtomicReference<Level> level;
  private final boolean autoFlush;
  private final Supplier<PrintWriter> writerSupplier;

  private final UnformattedLogWriter errorWriter;
  private final UnformattedLogWriter warnWriter;
  private final UnformattedLogWriter infoWriter;
  private final UnformattedLogWriter debugWriter;

  UnformattedLoggerImpl(Builder builder, Supplier<PrintWriter> writerSupplier) {
    this.locale = builder.locale;
    this.level = new AtomicReference<>(builder.level);
    this.autoFlush = builder.autoFlush;
    this.writerSupplier = writerSupplier;

    this.errorWriter = new UnformattedLogWriter(Level.ERROR, this, locale, writerSupplier, autoFlush);
    this.warnWriter = new UnformattedLogWriter(Level.WARN, this, locale, writerSupplier, autoFlush);
    this.infoWriter = new UnformattedLogWriter(Level.INFO, this, locale, writerSupplier, autoFlush);
    this.debugWriter = new UnformattedLogWriter(Level.DEBUG, this, locale, writerSupplier, autoFlush);
  }

  @Override
  public Level getLevel() {
    return this.level.get();
  }

  @Override
  public Level setLevel(Level level) {
    requireNonNull(level);
    return this.level.getAndSet(level);
  }

  @Override
  public boolean isEnabled(Level level) {
    requireNonNull(level);
    return level.compareTo(this.level.get()) <= 0;
  }

  @Override
  public LogWriter errorWriter() {
    return this.errorWriter;
  }

  @Override
  public LogWriter warnWriter() {
    return this.warnWriter;
  }

  @Override
  public LogWriter infoWriter() {
    return this.infoWriter;
  }

  @Override
  public LogWriter debugWriter() {
    return this.debugWriter;
  }

  @Override
  public void batch(Consumer<Logger> fn) {
    requireNonNull(fn);
    List<UnformattedLogEvent> events = new ArrayList<>(32);
    fn.accept(new BatchLogger(events::add));
    writeEvents(events);
  }

  void writeEvents(Collection<UnformattedLogEvent> logEvents) {
    if (logEvents.isEmpty()) {
      return;
    }
    Level currentLevel = this.level.get();
    PrintWriter out;
    synchronized (this) {
      out = writerSupplier.get();
      for (UnformattedLogEvent logEvent : logEvents) {
        if (logEvent.level().compareTo(currentLevel) > 0) {
          continue;
        }
        LogMessage message = logEvent.message();
        if (message != null) {
          try {
            message.appendTo(locale, out);
          } catch (IOException ex) {
            // PrintWriter does not throw this exception
            throw new RuntimeException("unexpected exception", ex);
          }
          out.println();
        } else {
          out.println(logEvent.formattedMessage(locale));
        }
        Throwable cause = logEvent.cause();
        if (cause != null) {
          cause.printStackTrace(out);
        }
      }
    }
    if (autoFlush) {
      out.flush();
    }
  }

  static final class BatchLogger implements Logger {
    private final LogWriter errorWriter;
    private final LogWriter warnWriter;
    private final LogWriter infoWriter;
    private final LogWriter debugWriter;

    BatchLogger(Consumer<UnformattedLogEvent> eventConsumer) {
      this.errorWriter =
          isEnabled(Level.ERROR) ? new BatchLogWriter(Level.ERROR, eventConsumer) : NullLogWriter.instance();
      this.warnWriter =
          isEnabled(Level.WARN) ? new BatchLogWriter(Level.WARN, eventConsumer) : NullLogWriter.instance();
      this.infoWriter =
          isEnabled(Level.INFO) ? new BatchLogWriter(Level.INFO, eventConsumer) : NullLogWriter.instance();
      this.debugWriter =
          isEnabled(Level.DEBUG) ? new BatchLogWriter(Level.DEBUG, eventConsumer) : NullLogWriter.instance();
    }

    @Override
    public Level getLevel() {
      return Level.DEBUG;
    }

    @Override
    public boolean isEnabled(Level level) {
      requireNonNull(level);
      return true;
    }

    @Override
    public LogWriter errorWriter() {
      return this.errorWriter;
    }

    @Override
    public LogWriter warnWriter() {
      return this.warnWriter;
    }

    @Override
    public LogWriter infoWriter() {
      return this.infoWriter;
    }

    @Override
    public LogWriter debugWriter() {
      return this.debugWriter;
    }

    @Override
    public void batch(Consumer<Logger> fn) {
      requireNonNull(fn);
      fn.accept(this);
    }
  }
}
