package uk.gov.ida.notification.exceptions.proxy;

import javax.ws.rs.WebApplicationException;

public class VerifyServiceProviderGenerateAuthnRequestResponseException extends WebApplicationException {
    private final String sessionId;

    public VerifyServiceProviderGenerateAuthnRequestResponseException(Throwable cause, String sessionId) {
        super(cause);
        this.sessionId = sessionId;
    }

    public String getSessionId() { return this.sessionId; }
}
