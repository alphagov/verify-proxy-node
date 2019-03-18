package uk.gov.ida.notification.exceptions.proxy;

import javax.ws.rs.WebApplicationException;

public class VerifyServiceProviderRequestException extends WebApplicationException {
    private final String sessionId;

    public VerifyServiceProviderRequestException(Throwable cause, String sessionId) {
        super(cause);
        this.sessionId = sessionId;
    }

    public String getSessionId() { return this.sessionId; }
}
