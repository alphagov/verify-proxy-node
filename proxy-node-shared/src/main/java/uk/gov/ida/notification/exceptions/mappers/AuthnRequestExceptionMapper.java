package uk.gov.ida.notification.exceptions.mappers;

import org.joda.time.DateTime;
import uk.gov.ida.notification.exceptions.authnrequest.AuthnRequestException;

import javax.ws.rs.core.Response;

public class AuthnRequestExceptionMapper extends BaseExceptionMapper<AuthnRequestException> {

    @Override
    protected Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }

    @Override
    protected String getResponseMessage(AuthnRequestException exception) {
        return "Error handling authn request. logId: " + getLogId();
    }

    @Override
    protected String getAuthnRequestId(AuthnRequestException exception) {
        return exception.getAuthnRequest().getID();
    }

    @Override
    protected String getIssuerId(AuthnRequestException exception) {
        return exception.getAuthnRequest().getIssuer().getValue();
    }

    @Override
    protected DateTime getIssueInstant(AuthnRequestException exception) {
        return exception.getAuthnRequest().getIssueInstant();
    }
}
