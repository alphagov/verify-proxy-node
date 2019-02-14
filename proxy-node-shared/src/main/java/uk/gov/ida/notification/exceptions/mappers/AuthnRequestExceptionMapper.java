package uk.gov.ida.notification.exceptions.mappers;

import io.dropwizard.jersey.errors.ErrorMessage;
import uk.gov.ida.notification.exceptions.authnrequest.AuthnRequestException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class AuthnRequestExceptionMapper extends BaseExceptionMapper<AuthnRequestException> {

    @Override
    protected void handleException(AuthnRequestException exception) {
        setAuthnRequestValues(
                exception.getAuthnRequest().getID(),
                exception.getAuthnRequest().getIssuer().getValue(),
                exception.getAuthnRequest().getIssueInstant()
        );
    }

    @Override
    protected Response getResponse(AuthnRequestException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "Error handling authn request. logId: " + getLogId()))
                .build();
    }
}
