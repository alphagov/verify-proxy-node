package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.SessionAlreadyExistsException;

import javax.ws.rs.core.Response;

public class SessionAlreadyExistsExceptionMapper extends ExceptionToErrorPageMapper<SessionAlreadyExistsException> {

    @Override
    protected Response.Status getResponseStatus(SessionAlreadyExistsException exception) {
        return Response.Status.BAD_REQUEST;
    }

    @Override
    protected String getErrorPageMessage(SessionAlreadyExistsException exception) {
        return "Something went wrong; session already exists";
    }
}
