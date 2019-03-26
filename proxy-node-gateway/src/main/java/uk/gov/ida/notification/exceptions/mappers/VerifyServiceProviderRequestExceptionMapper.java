package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.proxy.VerifyServiceProviderRequestException;

import javax.ws.rs.core.Response;
import java.net.URI;

public class VerifyServiceProviderRequestExceptionMapper extends ExceptionToErrorPageMapper<VerifyServiceProviderRequestException> {

    public VerifyServiceProviderRequestExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }

    @Override
    protected Response.Status getResponseStatus(VerifyServiceProviderRequestException exception) {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    @Override
    protected String getSessionId(VerifyServiceProviderRequestException exception) {
        return exception.getSessionId();
    }
}
