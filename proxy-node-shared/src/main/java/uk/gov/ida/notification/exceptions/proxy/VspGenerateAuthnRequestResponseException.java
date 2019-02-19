package uk.gov.ida.notification.exceptions.proxy;

import javax.ws.rs.WebApplicationException;

public class VspGenerateAuthnRequestResponseException extends WebApplicationException {
    private final String sessionId;

    public VspGenerateAuthnRequestResponseException(Throwable cause, String sessionId) {
        super(cause);
        this.sessionId = sessionId;
    }

    public String getSessionId() { return this.sessionId; }
}
