package uk.gov.ida.notification.exceptions.hubresponse;

import uk.gov.ida.notification.exceptions.mappers.ErrorJsonResponseException;

import javax.ws.rs.core.Response;

public class ResponseSigningException extends ErrorJsonResponseException {

    public ResponseSigningException(Throwable cause) {
        super("Failed to sign SAML message", cause);
    }

    @Override
    public Response.Status getResponseStatus() {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }
}
