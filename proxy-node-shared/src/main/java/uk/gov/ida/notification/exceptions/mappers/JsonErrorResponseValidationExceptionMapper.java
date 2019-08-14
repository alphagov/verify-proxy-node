package uk.gov.ida.notification.exceptions.mappers;

import io.dropwizard.jersey.validation.JerseyViolationException;

import javax.ws.rs.core.Response;

public class JsonErrorResponseValidationExceptionMapper extends BaseJsonErrorResponseExceptionMapper<JerseyViolationException> {

    @Override
    protected Response.Status getResponseStatus(JerseyViolationException e) {
        return Response.Status.BAD_REQUEST;
    }
}
