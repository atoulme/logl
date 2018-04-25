package org.logl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnformattedLoggerTest {

  private CharArrayWriter buffer;
  private PrintWriter out;

  @BeforeEach
  void setup() {
    buffer = new CharArrayWriter();
    out = new PrintWriter(buffer);
  }

  @Test
  void shouldOutputMessages() {
    LoggerProvider logProvider = UnformattedLogger.toPrintWriter(out);
    Logger logger = logProvider.getLogger("");

    logger.info("1output");
    logger.warn("2output");
    logger.debug("3output");
    // @formatter:off
    assertThat(buffer.toString()).isEqualTo(String.format(
        "1output%n" +
        "2output%n"));
    // @formatter:on
  }

  @Test
  void shouldOutputFormattedMessages() {
    LoggerProvider logProvider = UnformattedLogger.toPrintWriter(out);
    Logger logger = logProvider.getLogger("");

    logger.errorf("%d%s", 1, "output");
    logger.warn(LogMessage.messageFormat("{0}{1}", 2, "output"));
    logger.debug("3output");
    logger.info(LogMessage.stringFormat("4-%,d", 123456789));
    logger.info(LogMessage.patternFormat("{}{}", 5, "output"));
    logger.info("{}{}", 6, "output");
    // @formatter:off
    assertThat(buffer.toString()).isEqualTo(String.format(
        "1output%n" +
        "2output%n" +
        "4-123,456,789%n" +
        "5output%n" +
        "6output%n"));
    // @formatter:on
  }

  @Test
  void shouldOutputLocaleFormattedMessages() {
    LoggerProvider logProvider = UnformattedLogger.withLocale(Locale.GERMAN).toPrintWriter(out);
    Logger logger = logProvider.getLogger("");

    logger.errorf("%d%s", 1, "output");
    logger.warn(LogMessage.messageFormat("{0}{1}", 2, "output"));
    logger.debug("3output");
    logger.info(LogMessage.stringFormat("4-%,d", 123456789));
    logger.info(LogMessage.patternFormat("{}{}", 5, "output"));
    logger.info("{}{}", 6, "output");
    // @formatter:off
    assertThat(buffer.toString()).isEqualTo(String.format(
        "1output%n" +
        "2output%n" +
        "4-123.456.789%n" +
        "5output%n" +
        "6output%n"));
    // @formatter:on
  }

  @Test
  void shouldOutputExceptions() {
    LoggerProvider logProvider = UnformattedLogger.withLogLevel(Level.DEBUG).toPrintWriter(out);
    Logger logger = logProvider.getLogger("");

    RuntimeException exCause = new RuntimeException("Something happened");
    StackTraceElement causeElement1 = new StackTraceElement("org.logl.UnformattedLoggerTest", "fail", "foo.java", 12);
    StackTraceElement causeElement2 = new StackTraceElement("org.logl.UnformattedLoggerTest", "test", "foo.java", 14);
    StackTraceElement[] causeTrace = new StackTraceElement[] {causeElement1, causeElement2};
    exCause.setStackTrace(causeTrace);

    RuntimeException ex = new RuntimeException("Something happened", exCause);
    StackTraceElement element1 = new StackTraceElement("org.logl.UnformattedLoggerTest", "foo", "foo.java", 63);
    StackTraceElement element2 = new StackTraceElement("org.logl.UnformattedLoggerTest", "bar", "foo.java", 32);
    StackTraceElement[] stackTrace = new StackTraceElement[] {element1, element2};
    ex.setStackTrace(stackTrace);

    logger.errorf("%d%s", 1, "output");
    logger.info("{}{}", 2, "output");
    logger.debug("3output", ex);
    // @formatter:off
    assertThat(buffer.toString()).isEqualTo(String.format(
        "1output%n" +
        "2output%n" +
        "3output%n" +
        "java.lang.RuntimeException: Something happened%n" +
        "\tat org.logl.UnformattedLoggerTest.foo(foo.java:63)%n" +
        "\tat org.logl.UnformattedLoggerTest.bar(foo.java:32)%n" +
        "Caused by: java.lang.RuntimeException: Something happened%n" +
        "\tat org.logl.UnformattedLoggerTest.fail(foo.java:12)%n" +
        "\tat org.logl.UnformattedLoggerTest.test(foo.java:14)%n"));
    // @formatter:on
  }

  @Test
  void shouldOutputBatchedMessages() {
    AdjustableLoggerProvider logProvider = UnformattedLogger.toPrintWriter(out);
    AdjustableLogger logger = logProvider.getLogger("");

    RuntimeException exCause = new RuntimeException("Something happened");
    StackTraceElement causeElement1 = new StackTraceElement("org.logl.UnformattedLoggerTest", "fail", "foo.java", 12);
    StackTraceElement causeElement2 = new StackTraceElement("org.logl.UnformattedLoggerTest", "test", "foo.java", 14);
    StackTraceElement[] causeTrace = new StackTraceElement[] {causeElement1, causeElement2};
    exCause.setStackTrace(causeTrace);

    RuntimeException ex = new RuntimeException("Something happened", exCause);
    StackTraceElement element1 = new StackTraceElement("org.logl.UnformattedLoggerTest", "foo", "foo.java", 63);
    StackTraceElement element2 = new StackTraceElement("org.logl.UnformattedLoggerTest", "bar", "foo.java", 32);
    StackTraceElement[] stackTrace = new StackTraceElement[] {element1, element2};
    ex.setStackTrace(stackTrace);

    logger.setLevel(Level.WARN);

    logger.errorf("%d%s", 1, "output");
    logger.info("{}{}", 2, "output");
    logger.batch(batchLogger -> {
      batchLogger.info("b1output");
      logger.info("3output");
      batchLogger.warn("b2{}", "output");
      logger.setLevel(Level.INFO);
    });
    logger.info("4output", ex);
    // @formatter:off
    assertThat(buffer.toString()).isEqualTo(String.format(
        "1output%n" +
        "b1output%n" +
        "b2output%n" +
        "4output%n" +
        "java.lang.RuntimeException: Something happened%n" +
        "\tat org.logl.UnformattedLoggerTest.foo(foo.java:63)%n" +
        "\tat org.logl.UnformattedLoggerTest.bar(foo.java:32)%n" +
        "Caused by: java.lang.RuntimeException: Something happened%n" +
        "\tat org.logl.UnformattedLoggerTest.fail(foo.java:12)%n" +
        "\tat org.logl.UnformattedLoggerTest.test(foo.java:14)%n"));
    // @formatter:on
  }
}
