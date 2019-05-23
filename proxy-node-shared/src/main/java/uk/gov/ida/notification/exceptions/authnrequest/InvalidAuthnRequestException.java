package uk.gov.ida.notification.exceptions.authnrequest;

import uk.gov.ida.notification.exceptions.mappers.BaseJsonErrorResponseRuntimeException;

import javax.ws.rs.core.Response;

public class InvalidAuthnRequestException extends BaseJsonErrorResponseRuntimeException {

    private static final String EXCEPTION_PREFIX = "Bad Authn Request from Connector Node: ";

    public InvalidAuthnRequestException(String message) {
        super(EXCEPTION_PREFIX + message);
    }

    public InvalidAuthnRequestException(String message, Throwable e) {
        super(EXCEPTION_PREFIX + message, e);
    }

    @Override
    public Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }
}
