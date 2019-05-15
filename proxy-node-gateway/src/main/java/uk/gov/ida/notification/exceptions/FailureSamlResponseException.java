package uk.gov.ida.notification.exceptions;

import javax.ws.rs.core.Response;

public abstract class FailureSamlResponseException extends RuntimeException {
    FailureSamlResponseException(Throwable cause) {
        super(cause);
    }

    FailureSamlResponseException(String message) {
        super(message);
    }

    public abstract Response.Status getResponseStatus();
}
