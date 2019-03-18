package uk.gov.ida.notification.exceptions.mappers;

import javax.ws.rs.core.Response;

public class GenericExceptionMapper extends ExceptionToErrorPageMapper<Exception> {

    @Override
    protected Response.Status getResponseStatus(Exception exception) {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    @Override
    protected String getErrorPageMessage(Exception exception) {
        return exception.getMessage();
    }
}
