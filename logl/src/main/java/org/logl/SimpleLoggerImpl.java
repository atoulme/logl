package org.logl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.logl.SimpleLogWriter.BatchLogWriter;
import org.logl.SimpleLogger.Builder;

final class SimpleLoggerImpl implements AdjustableLogger {

  private final String name;
  private final AtomicReference<Level> level;
  private final Supplier<Instant> currentTimeSupplier;
  private final ZoneId zone;
  private final Locale locale;
  private final boolean autoFlush;
  private final Supplier<PrintWriter> writerSupplier;

  private final SimpleLogWriter errorWriter;
  private final SimpleLogWriter warnWriter;
  private final SimpleLogWriter infoWriter;
  private final SimpleLogWriter debugWriter;

  SimpleLoggerImpl(String name, Builder builder, Supplier<PrintWriter> writerSupplier) {
    this.name = NameAbbreviator.forPattern("1.").abbreviate(name);
    this.level = new AtomicReference<>(builder.level);
    this.currentTimeSupplier = builder.currentTimeSupplier;
    this.zone = builder.zone;
    this.locale = builder.locale;
    this.autoFlush = builder.autoFlush;
    this.writerSupplier = writerSupplier;

    this.errorWriter = new SimpleLogWriter(Level.ERROR, this, currentTimeSupplier);
    this.warnWriter = new SimpleLogWriter(Level.WARN, this, currentTimeSupplier);
    this.infoWriter = new SimpleLogWriter(Level.INFO, this, currentTimeSupplier);
    this.debugWriter = new SimpleLogWriter(Level.DEBUG, this, currentTimeSupplier);
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

  void log(Level level, LogMessage message) {
    if (!isEnabled(level)) {
      return;
    }
    Instant now = currentTimeSupplier.get();
    PrintWriter out;
    synchronized (this) {
      out = writerSupplier.get();
      writePrefix(out, now, level);
      writeMessage(out, message);
      out.println();
    }
  }

  void log(Level level, CharSequence message) {
    if (!isEnabled(level)) {
      return;
    }
    Instant now = currentTimeSupplier.get();
    PrintWriter out;
    synchronized (this) {
      out = writerSupplier.get();
      writePrefix(out, now, level);
      out.print(message);
      out.println();
    }
  }

  void log(Level level, LogMessage message, Throwable cause) {
    if (cause == null) {
      log(level, message);
      return;
    }
    if (!isEnabled(level)) {
      return;
    }
    Instant now = currentTimeSupplier.get();
    PrintWriter out;
    synchronized (this) {
      out = writerSupplier.get();
      writePrefix(out, now, level);
      writeMessage(out, message);
      out.println();
      cause.printStackTrace(out);
    }
  }

  void log(Level level, CharSequence message, Throwable cause) {
    if (cause == null) {
      log(level, message);
      return;
    }
    if (!isEnabled(level)) {
      return;
    }
    Instant now = currentTimeSupplier.get();
    PrintWriter out;
    synchronized (this) {
      out = writerSupplier.get();
      writePrefix(out, now, level);
      out.print(message);
      out.println();
      cause.printStackTrace(out);
    }
  }

  void logf(Level level, String format, Object... args) {
    if (!isEnabled(level)) {
      return;
    }
    Instant now = currentTimeSupplier.get();
    PrintWriter out;
    synchronized (this) {
      out = writerSupplier.get();
      writePrefix(out, now, level);
      out.printf(format, args);
      out.println();
    }
  }

  void writeEvents(Collection<SimpleLogEvent> logEvents) {
    if (logEvents.isEmpty()) {
      return;
    }
    Level currentLevel = this.level.get();
    PrintWriter out;
    synchronized (this) {
      out = writerSupplier.get();
      for (SimpleLogEvent logEvent : logEvents) {
        Level level = logEvent.level();
        if (level.compareTo(currentLevel) > 0) {
          continue;
        }

        writePrefix(out, logEvent.time(), level);

        LogMessage message = logEvent.message();
        if (message != null) {
          writeMessage(out, message);
        } else {
          out.print(logEvent.formattedMessage(locale));
        }
        out.println();

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

  private void writePrefix(PrintWriter out, Instant now, Level level) {
    DateFormatter.formatTo(now.atZone(zone), out);
    String lname = level.name();
    out.write("  ", 0, 6 - lname.length());
    out.write(lname);
    out.write(" [");
    out.write(name);
    out.write("] ");
  }

  private void writeMessage(PrintWriter out, LogMessage message) {
    try {
      message.appendTo(locale, out);
    } catch (IOException ex) {
      // PrintWriter does not throw this exception
      throw new RuntimeException("unexpected exception", ex);
    }
  }

  @Override
  public void batch(Consumer<Logger> fn) {
    requireNonNull(fn);
    List<SimpleLogEvent> events = new ArrayList<>(32);
    fn.accept(new BatchLogger(events::add));
    writeEvents(events);
  }

  final class BatchLogger implements Logger {
    private final LogWriter errorWriter;
    private final LogWriter warnWriter;
    private final LogWriter infoWriter;
    private final LogWriter debugWriter;

    BatchLogger(Consumer<SimpleLogEvent> eventConsumer) {
      this.errorWriter = isEnabled(Level.ERROR) ? new BatchLogWriter(Level.ERROR, currentTimeSupplier, eventConsumer)
          : NullLogWriter.instance();
      this.warnWriter = isEnabled(Level.WARN) ? new BatchLogWriter(Level.WARN, currentTimeSupplier, eventConsumer)
          : NullLogWriter.instance();
      this.infoWriter = isEnabled(Level.INFO) ? new BatchLogWriter(Level.INFO, currentTimeSupplier, eventConsumer)
          : NullLogWriter.instance();
      this.debugWriter = isEnabled(Level.DEBUG) ? new BatchLogWriter(Level.DEBUG, currentTimeSupplier, eventConsumer)
          : NullLogWriter.instance();
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
