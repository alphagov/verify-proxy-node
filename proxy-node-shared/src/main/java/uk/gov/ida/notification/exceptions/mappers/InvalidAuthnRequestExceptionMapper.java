package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

import javax.ws.rs.core.Response;

import static java.text.MessageFormat.format;

public class InvalidAuthnRequestExceptionMapper extends BaseExceptionMapper<InvalidAuthnRequestException> {

    @Override
    protected Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }

    @Override
    protected String getResponseMessage(InvalidAuthnRequestException exception) {
        return format("ESP InvalidAuthnRequestException: {0}.", exception.getMessage());
    }
}
