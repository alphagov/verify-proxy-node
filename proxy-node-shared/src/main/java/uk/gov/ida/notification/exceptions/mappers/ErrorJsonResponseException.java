package uk.gov.ida.notification.exceptions.mappers;

import javax.ws.rs.core.Response;

public abstract class ErrorJsonResponseException extends RuntimeException {

    public ErrorJsonResponseException(String message) {
        super(message);
    }

    public ErrorJsonResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract Response.Status getResponseStatus();
}
