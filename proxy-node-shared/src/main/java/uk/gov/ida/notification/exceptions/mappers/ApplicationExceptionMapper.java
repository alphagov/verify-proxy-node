package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.exceptions.ApplicationException;

import javax.ws.rs.core.Response;

import static java.text.MessageFormat.format;

public class ApplicationExceptionMapper extends BaseExceptionMapper<ApplicationException> {

    @Override
    protected void handleException(ApplicationException exception) { }

    @Override
    protected Response getResponse(ApplicationException exception) {
        final String message = format("Exception with id {0} of type {1} whilst contacting uri [{2}]: {3}",
                exception.getErrorId(), exception.getExceptionType(), exception.getUri(), exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    }
}
