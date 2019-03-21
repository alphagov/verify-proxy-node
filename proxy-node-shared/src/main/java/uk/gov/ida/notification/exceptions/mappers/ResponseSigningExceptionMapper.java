package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.hubresponse.ResponseSigningException;

import javax.ws.rs.core.Response;
import java.text.MessageFormat;

public class ResponseSigningExceptionMapper extends BaseExceptionMapper<ResponseSigningException> {

    @Override
    protected Response.Status getResponseStatus() {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    @Override
    protected String getResponseMessage(ResponseSigningException exception) {
        return MessageFormat.format("{0}; {1}", exception.getMessage(), exception.getCause().getMessage());
    }
}
