package org.logl;

import java.io.IOException;
import java.util.Locale;

final class UnformattedLogEvent {
  private final Level level;
  private final CharSequence formattedMessage;
  private final LogMessage message;
  private final Throwable cause;

  UnformattedLogEvent(Level level, CharSequence formattedMessage, Throwable cause) {
    this.level = level;
    this.formattedMessage = formattedMessage;
    this.message = null;
    this.cause = cause;
  }

  UnformattedLogEvent(Level level, LogMessage message, Throwable cause) {
    this.level = level;
    this.formattedMessage = null;
    this.message = message;
    this.cause = cause;
  }

  Level level() {
    return level;
  }

  CharSequence formattedMessage(Locale locale) {
    if (formattedMessage != null) {
      return formattedMessage;
    }

    StringBuilder builder = new StringBuilder(32);
    try {
      message.appendTo(locale, builder);
    } catch (IOException ex) {
      // StringBuilder doesn't throw this exception
      throw new RuntimeException("Unexpected exception", ex);
    }
    return builder;
  }

  LogMessage message() {
    return message;
  }

  Throwable cause() {
    return cause;
  }
}
