package uk.gov.ida.eidas.metatron.exceptions;

import io.dropwizard.jersey.errors.ErrorMessage;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class MetatronClientExceptionMapper implements ExceptionMapper<MetatronClientException> {
    @Override
    public Response toResponse(MetatronClientException exception) {
        ProxyNodeLogger.logException(exception, "Metatron client exception");
        ErrorMessage errorMessage = new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), exception.getMessage());
        return Response
                .status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(errorMessage)
                .build();
    }
}
