package uk.gov.ida.notification.exceptions;

import javax.ws.rs.WebApplicationException;

public class EidasSamlParserResponseException extends WebApplicationException{
    private final String sessionId;

    public EidasSamlParserResponseException(Throwable cause, String sessionId) {
        super(cause);
        this.sessionId = sessionId;
    }

    public String getSessionId() { return this.sessionId; }
}
