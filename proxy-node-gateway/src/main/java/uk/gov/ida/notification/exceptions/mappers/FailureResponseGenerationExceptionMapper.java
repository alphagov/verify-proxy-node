package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.FailureResponseGenerationException;

import javax.ws.rs.core.Response;
import java.net.URI;

public class FailureResponseGenerationExceptionMapper extends ExceptionToErrorPageMapper<FailureResponseGenerationException> {

    public FailureResponseGenerationExceptionMapper(URI errorPageRedirectUrl) {
        super(errorPageRedirectUrl);
    }

    @Override
    protected Response.Status getResponseStatus(FailureResponseGenerationException exception) {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    @Override
    protected String getAuthnRequestId(FailureResponseGenerationException exception) {
        return exception.getRequestId();
    }
}
