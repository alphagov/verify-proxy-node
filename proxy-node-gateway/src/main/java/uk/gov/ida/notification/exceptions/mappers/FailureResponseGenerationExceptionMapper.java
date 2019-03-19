package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.FailureResponseGenerationException;

import javax.ws.rs.core.Response;

public class FailureResponseGenerationExceptionMapper extends ExceptionToErrorPageMapper<FailureResponseGenerationException> {

    @Override
    protected Response.Status getResponseStatus(FailureResponseGenerationException exception) {
        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    @Override
    protected String getErrorPageMessage(FailureResponseGenerationException exception) {
        return "Failed to generate failure response";
    }

    @Override
    protected String getAuthnRequestId(FailureResponseGenerationException exception) {
        return exception.getRequestId();
    }
}
