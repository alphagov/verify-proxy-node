package uk.gov.ida.notification.exceptions.mappers;

import io.dropwizard.jersey.errors.ErrorMessage;
import uk.gov.ida.notification.shared.ProxyNodeLogger;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Level;

import static java.text.MessageFormat.format;

public abstract class BaseJsonErrorResponseExceptionMapper<TException extends Throwable> implements ExceptionMapper<TException> {

    private UriInfo uriInfo;

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Response toResponse(TException exception) {
        final Response.Status responseStatus = getResponseStatus(exception);
        final String message = getResponseMessage(exception);

        ProxyNodeLogger.logException(exception, Level.WARNING,
                format("Error whilst contacting URI {0}: {1}", uriInfo.getPath(), message));

        return Response.status(responseStatus)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorMessage(
                        responseStatus.getStatusCode(),
                        message))
                .build();
    }

    protected abstract Response.Status getResponseStatus(TException exception);

    protected String getResponseMessage(TException exception) {
        return exception.getMessage();
    }
}
