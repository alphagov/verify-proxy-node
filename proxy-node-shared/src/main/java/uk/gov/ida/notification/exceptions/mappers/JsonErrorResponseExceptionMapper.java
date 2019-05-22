package uk.gov.ida.notification.exceptions.mappers;

import javax.ws.rs.core.Response;

public class JsonErrorResponseExceptionMapper extends BaseJsonErrorResponseExceptionMapper<ErrorJsonResponseException> {

    @Override
    protected Response.Status getResponseStatus(ErrorJsonResponseException exception) {
        return exception.getResponseStatus();
    }
}
