package uk.gov.ida.notification.exceptions;

import javax.ws.rs.WebApplicationException;

public class FailureResponseGenerationException extends WebApplicationException {
    private final String requestId;

    public FailureResponseGenerationException(Throwable cause) {
        this(cause, null);
    }

    public FailureResponseGenerationException(Throwable cause, String requestId) {
        super(cause);
        this.requestId = requestId;
    }

    public String getRequestId() { return this.requestId; }
}
