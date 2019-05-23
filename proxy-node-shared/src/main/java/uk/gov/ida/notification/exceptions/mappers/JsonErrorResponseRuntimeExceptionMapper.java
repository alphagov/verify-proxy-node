package uk.gov.ida.notification.exceptions.mappers;

import javax.ws.rs.core.Response;

public class JsonErrorResponseRuntimeExceptionMapper extends BaseJsonErrorResponseExceptionMapper<BaseJsonErrorResponseRuntimeException> {

    @Override
    protected Response.Status getResponseStatus(BaseJsonErrorResponseRuntimeException exception) {
        return exception.getResponseStatus();
    }
}
