package uk.gov.ida.notification.exceptions.mappers;

import javax.ws.rs.core.Response;

public class GenericExceptionMapper extends BaseJsonErrorResponseExceptionMapper<Exception> {

    @Override
    protected Response.Status getResponseStatus(Exception e) {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }
}
