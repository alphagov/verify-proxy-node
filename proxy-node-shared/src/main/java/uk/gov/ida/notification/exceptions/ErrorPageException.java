package uk.gov.ida.notification.exceptions;

import java.util.logging.Level;

public abstract class ErrorPageException extends RuntimeException {

    ErrorPageException(String message) {
        super(message);
    }

    public ErrorPageException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ErrorPageException(Throwable cause) {
        super(cause);
    }

    public Level getLogLevel() {
        return Level.WARNING;
    }
}
