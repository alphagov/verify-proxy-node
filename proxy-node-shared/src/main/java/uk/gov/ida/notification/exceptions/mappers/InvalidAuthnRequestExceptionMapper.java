package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import javax.ws.rs.core.Response;
import static java.text.MessageFormat.format;

public class InvalidAuthnRequestExceptionMapper extends BaseExceptionMapper<InvalidAuthnRequestException> {

    @Override
    protected void handleException(InvalidAuthnRequestException exception) {
    }

    @Override
    protected Response getResponse(InvalidAuthnRequestException exception) {
        final String message = format("ESP InvalidAuthnRequestException: {0}.", exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }
}
