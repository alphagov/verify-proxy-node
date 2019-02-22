package uk.gov.ida.notification.exceptions;

import javax.ws.rs.WebApplicationException;

public class TranslatorResponseException extends WebApplicationException {
    private final String sessionId;

    public TranslatorResponseException(Throwable cause, String sessionId) {
        super(cause);
        this.sessionId = sessionId;
    }

    public String getSessionId() { return this.sessionId; }
}
