package uk.gov.ida.notification.exceptions;

import javax.ws.rs.WebApplicationException;

public class SessionAttributeException extends WebApplicationException {
    public SessionAttributeException(Throwable cause) {
        super(cause);
    }
}
