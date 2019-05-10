package uk.gov.ida.notification.exceptions;

import java.util.logging.Level;

public abstract class EidasErrorPageException extends RuntimeException {

    EidasErrorPageException(String message) {
        super(message);
    }

    protected EidasErrorPageException(Throwable cause) {
        super(cause);
    }

    public Level getLogLevel() {
        return Level.WARNING;
    }
}
