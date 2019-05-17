package uk.gov.ida.notification.exceptions.hubresponse;

import uk.gov.ida.notification.exceptions.mappers.ErrorJsonResponseException;

import javax.ws.rs.core.Response;

public class ResponseAssertionEncryptionException extends ErrorJsonResponseException {

    public ResponseAssertionEncryptionException(Throwable cause) {
        super("Failed to encrypt assertion", cause);
    }

    @Override
    public Response.Status getResponseStatus() {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }
}
