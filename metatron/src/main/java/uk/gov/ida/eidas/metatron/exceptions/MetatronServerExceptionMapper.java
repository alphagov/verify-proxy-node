package uk.gov.ida.eidas.metatron.exceptions;

import io.dropwizard.jersey.errors.ErrorMessage;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class MetatronServerExceptionMapper implements ExceptionMapper<MetatronServerException> {
    @Override
    public Response toResponse(MetatronServerException exception) {
        ProxyNodeLogger.logException(exception, "Metatron server exception");
        ErrorMessage errorMessage = new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getMessage());
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(errorMessage)
                .build();
    }
}
