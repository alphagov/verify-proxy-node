package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.exceptions.ApplicationException;

import javax.ws.rs.core.Response;

import static java.text.MessageFormat.format;

public class ApplicationExceptionMapper extends BaseJsonErrorResponseExceptionMapper<ApplicationException> {

    @Override
    protected Response.Status getResponseStatus(ApplicationException exception) {
        return Response.Status.BAD_REQUEST;
    }

    @Override
    protected String getResponseMessage(ApplicationException exception) {
        return format("Exception with id {0} of type {1} whilst contacting uri [{2}]: {3}",
                exception.getErrorId(), exception.getExceptionType(), exception.getUri(), exception.getMessage());
    }
}
