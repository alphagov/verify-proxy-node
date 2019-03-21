package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.SessionMissingException;

import javax.ws.rs.core.Response;

public class SessionMissingExceptionMapper extends ExceptionToErrorPageMapper<SessionMissingException> {

    @Override
    protected Response.Status getResponseStatus(SessionMissingException exception) {
        return Response.Status.BAD_REQUEST;
    }

    @Override
    protected String getErrorPageMessage(SessionMissingException exception) {
        return "Something went wrong; session does not exist";
    }
}
