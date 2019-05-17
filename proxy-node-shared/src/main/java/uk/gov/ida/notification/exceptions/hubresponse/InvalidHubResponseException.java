package uk.gov.ida.notification.exceptions.hubresponse;

import uk.gov.ida.notification.exceptions.mappers.ErrorJsonResponseException;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;

import javax.ws.rs.core.Response;

public class InvalidHubResponseException extends ErrorJsonResponseException {

    private static final String EXCEPTION_PREFIX = "Bad IDP Response from Hub: ";

    public InvalidHubResponseException(String message) {
        super(EXCEPTION_PREFIX + message);
    }

    public InvalidHubResponseException(String message, SamlTransformationErrorException exception) {
        super(EXCEPTION_PREFIX + message, exception);
    }

    @Override
    public Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }
}
