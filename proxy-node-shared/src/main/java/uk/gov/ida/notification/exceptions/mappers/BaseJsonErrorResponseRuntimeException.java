package uk.gov.ida.notification.exceptions.mappers;

import javax.ws.rs.core.Response;

public abstract class BaseJsonErrorResponseRuntimeException extends RuntimeException {

    public BaseJsonErrorResponseRuntimeException(String message) {
        super(message);
    }

    public BaseJsonErrorResponseRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract Response.Status getResponseStatus();
}
