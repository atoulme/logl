package org.logl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleLoggerTest {

  private CharArrayWriter buffer;
  private PrintWriter out;
  private Instant now = Instant.parse("2007-12-03T10:15:30.00Z");

  @BeforeEach
  void setup() {
    buffer = new CharArrayWriter();
    out = new PrintWriter(buffer);
  }

  @Test
  void shouldOutputMessages() {
    LoggerProvider logProvider = SimpleLogger.usingCurrentTimeSupplier(() -> now).toPrintWriter(out);
    Logger logger = logProvider.getLogger("org.logl.StandardLoggerTest");

    logger.info("1output");
    now = now.plusMillis(700);
    logger.warn("2output");
    logger.debug("3output");
    // @formatter:off
    assertThat(buffer.toString()).isEqualTo(String.format(
        "2007-12-03 10:15:30.000+0000  INFO [o.l.StandardLoggerTest] 1output%n" +
        "2007-12-03 10:15:30.700+0000  WARN [o.l.StandardLoggerTest] 2output%n"));
    // @formatter:on
  }

  @Test
  void shouldOutputFormattedMessages() {
    LoggerProvider logProvider = SimpleLogger.usingCurrentTimeSupplier(() -> now).toPrintWriter(out);
    Logger logger = logProvider.getLogger(getClass());

    logger.errorf("%d%s", 1, "output");
    now = now.plusMillis(1400);
    logger.warn(LogMessage.messageFormat("{0}{1}", 2, "output"));
    now = now.plusMillis(1400);
    logger.debug("3output");
    now = now.plusMillis(1400);
    logger.info(LogMessage.stringFormat("4-%,d", 123456789));
    logger.info(LogMessage.patternFormat("{}{}", 5, "output"));
    logger.info("{}{}", 6, "output");
    // @formatter:off
    assertThat(buffer.toString()).isEqualTo(String.format(
        "2007-12-03 10:15:30.000+0000 ERROR [o.l.SimpleLoggerTest] 1output%n" +
        "2007-12-03 10:15:31.400+0000  WARN [o.l.SimpleLoggerTest] 2output%n" +
        "2007-12-03 10:15:34.200+0000  INFO [o.l.SimpleLoggerTest] 4-123,456,789%n" +
        "2007-12-03 10:15:34.200+0000  INFO [o.l.SimpleLoggerTest] 5output%n" +
        "2007-12-03 10:15:34.200+0000  INFO [o.l.SimpleLoggerTest] 6output%n"));
    // @formatter:on
  }

  @Test
  void shouldOutputLocaleFormattedMessages() {
    LoggerProvider logProvider =
        SimpleLogger.usingCurrentTimeSupplier(() -> now).withLocale(Locale.GERMAN).toPrintWriter(out);
    Logger logger = logProvider.getLogger("");

    logger.errorf("%d%s", 1, "output");
    now = now.plusMillis(900);
    logger.warn(LogMessage.messageFormat("{0}{1}", 2, "output"));
    now = now.plusMillis(900);
    logger.debug("3output");
    now = now.plusMillis(900);
    logger.info(LogMessage.stringFormat("4-%,d", 123456789));
    logger.info(LogMessage.patternFormat("{}{}", 5, "output"));
    logger.info("{}{}", 6, "output");
    // @formatter:off
    assertThat(buffer.toString()).isEqualTo(String.format(
        "2007-12-03 10:15:30.000+0000 ERROR [] 1output%n" +
        "2007-12-03 10:15:30.900+0000  WARN [] 2output%n" +
        "2007-12-03 10:15:32.700+0000  INFO [] 4-123.456.789%n" +
        "2007-12-03 10:15:32.700+0000  INFO [] 5output%n" +
        "2007-12-03 10:15:32.700+0000  INFO [] 6output%n"));
    // @formatter:on
  }

  @Test
  void shouldOutputExceptions() {
    LoggerProvider logProvider =
        SimpleLogger.usingCurrentTimeSupplier(() -> now).withLogLevel(Level.DEBUG).toPrintWriter(out);
    Logger logger = logProvider.getLogger(SimpleLogger.class);

    RuntimeException exCause = new RuntimeException("Something happened");
    StackTraceElement causeElement1 = new StackTraceElement("org.logl.SimpleLoggerTest", "fail", "foo.java", 12);
    StackTraceElement causeElement2 = new StackTraceElement("org.logl.SimpleLoggerTest", "test", "foo.java", 14);
    StackTraceElement[] causeTrace = new StackTraceElement[] {causeElement1, causeElement2};
    exCause.setStackTrace(causeTrace);

    RuntimeException ex = new RuntimeException("Something happened", exCause);
    StackTraceElement element1 = new StackTraceElement("org.logl.SimpleLoggerTest", "foo", "foo.java", 63);
    StackTraceElement element2 = new StackTraceElement("org.logl.SimpleLoggerTest", "bar", "foo.java", 32);
    StackTraceElement[] stackTrace = new StackTraceElement[] {element1, element2};
    ex.setStackTrace(stackTrace);

    logger.errorf("%d%s", 1, "output");
    logger.info("{}{}", 2, "output");
    logger.debug("3output", ex);
    // @formatter:off
    assertThat(buffer.toString()).isEqualTo(String.format(
        "2007-12-03 10:15:30.000+0000 ERROR [o.l.SimpleLogger] 1output%n" +
        "2007-12-03 10:15:30.000+0000  INFO [o.l.SimpleLogger] 2output%n" +
        "2007-12-03 10:15:30.000+0000 DEBUG [o.l.SimpleLogger] 3output%n" +
        "java.lang.RuntimeException: Something happened%n" +
        "\tat org.logl.SimpleLoggerTest.foo(foo.java:63)%n" +
        "\tat org.logl.SimpleLoggerTest.bar(foo.java:32)%n" +
        "Caused by: java.lang.RuntimeException: Something happened%n" +
        "\tat org.logl.SimpleLoggerTest.fail(foo.java:12)%n" +
        "\tat org.logl.SimpleLoggerTest.test(foo.java:14)%n"));
    // @formatter:on
  }

  @Test
  void shouldOutputBatchedMessages() {
    AdjustableLoggerProvider logProvider = SimpleLogger.usingCurrentTimeSupplier(() -> now).toPrintWriter(out);
    AdjustableLogger logger = logProvider.getLogger(SimpleLogger.class);

    RuntimeException exCause = new RuntimeException("Something happened");
    StackTraceElement causeElement1 = new StackTraceElement("org.logl.SimpleLoggerTest", "fail", "foo.java", 12);
    StackTraceElement causeElement2 = new StackTraceElement("org.logl.SimpleLoggerTest", "test", "foo.java", 14);
    StackTraceElement[] causeTrace = new StackTraceElement[] {causeElement1, causeElement2};
    exCause.setStackTrace(causeTrace);

    RuntimeException ex = new RuntimeException("Something happened", exCause);
    StackTraceElement element1 = new StackTraceElement("org.logl.SimpleLoggerTest", "foo", "foo.java", 63);
    StackTraceElement element2 = new StackTraceElement("org.logl.SimpleLoggerTest", "bar", "foo.java", 32);
    StackTraceElement[] stackTrace = new StackTraceElement[] {element1, element2};
    ex.setStackTrace(stackTrace);

    logger.setLevel(Level.WARN);

    logger.errorf("%d%s", 1, "output");
    now = now.plusMillis(900);
    logger.info("{}{}", 2, "output");
    now = now.plusMillis(900);
    logger.batch(batchLogger -> {
      batchLogger.info("b1output");
      now = now.plusMillis(900);
      logger.info("3output");
      now = now.plusMillis(900);
      batchLogger.warn("b2{}", "output");
      now = now.plusMillis(900);
      logger.setLevel(Level.INFO);
    });
    logger.info("4output", ex);
    // @formatter:off
    assertThat(buffer.toString()).isEqualTo(String.format(
        "2007-12-03 10:15:30.000+0000 ERROR [o.l.SimpleLogger] 1output%n" +
        "2007-12-03 10:15:31.800+0000  INFO [o.l.SimpleLogger] b1output%n" +
        "2007-12-03 10:15:33.600+0000  WARN [o.l.SimpleLogger] b2output%n" +
        "2007-12-03 10:15:34.500+0000  INFO [o.l.SimpleLogger] 4output%n" +
        "java.lang.RuntimeException: Something happened%n" +
        "\tat org.logl.SimpleLoggerTest.foo(foo.java:63)%n" +
        "\tat org.logl.SimpleLoggerTest.bar(foo.java:32)%n" +
        "Caused by: java.lang.RuntimeException: Something happened%n" +
        "\tat org.logl.SimpleLoggerTest.fail(foo.java:12)%n" +
        "\tat org.logl.SimpleLoggerTest.test(foo.java:14)%n"));
    // @formatter:on
  }
}
