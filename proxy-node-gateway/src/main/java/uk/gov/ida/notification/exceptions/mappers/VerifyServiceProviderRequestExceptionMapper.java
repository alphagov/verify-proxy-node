package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.proxy.VerifyServiceProviderRequestException;

import javax.ws.rs.core.Response;

public class VerifyServiceProviderRequestExceptionMapper extends ExceptionToErrorPageMapper<VerifyServiceProviderRequestException> {

    @Override
    protected Response.Status getResponseStatus(VerifyServiceProviderRequestException exception) {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    @Override
    protected String getErrorPageMessage(VerifyServiceProviderRequestException exception) {
        return "Something went wrong when contacting the VSP";
    }

    @Override
    protected String getSessionId(VerifyServiceProviderRequestException exception) {
        return exception.getSessionId();
    }
}
