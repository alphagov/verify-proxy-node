package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.SessionAttributeException;

import javax.ws.rs.core.Response;

public class SessionAttributeExceptionMapper extends ExceptionToErrorPageMapper<SessionAttributeException> {

    @Override
    protected Response.Status getResponseStatus(SessionAttributeException exception) {
        return Response.Status.BAD_REQUEST;
    }

    @Override
    protected String getErrorPageMessage(SessionAttributeException exception) {
        return "Something went wrong; invalid session attributes";
    }
}
