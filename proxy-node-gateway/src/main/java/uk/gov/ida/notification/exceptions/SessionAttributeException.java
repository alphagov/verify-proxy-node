package uk.gov.ida.notification.exceptions;

import javax.ws.rs.WebApplicationException;

public class SessionAttributeException extends WebApplicationException {
    private final String sessionId;

    public SessionAttributeException(String message, String sessionId) {
        super(message);
        this.sessionId = sessionId;
    }

    public String getSessionId() { return this.sessionId; }
}
