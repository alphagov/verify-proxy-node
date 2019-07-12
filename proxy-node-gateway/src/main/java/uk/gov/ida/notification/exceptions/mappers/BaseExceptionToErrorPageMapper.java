package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.net.URI;
import java.util.logging.Level;

import static java.text.MessageFormat.format;

public abstract class BaseExceptionToErrorPageMapper<TException extends Exception> implements ExceptionMapper<TException> {

    private final URI errorPageRedirectUrl;

    private UriInfo uriInfo;

    BaseExceptionToErrorPageMapper(URI errorPageRedirectUrl) {
        this.errorPageRedirectUrl = errorPageRedirectUrl;
    }

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Response toResponse(TException exception) {
        logException(exception);
        return Response.seeOther(errorPageRedirectUrl).build();
    }

    public abstract Level getLogLevel(TException exception);

    private void logException(TException exception) {
        ProxyNodeLogger.logException(exception, getLogLevel(exception),
                format("Error whilst contacting URI [{0}]", this.uriInfo.getAbsolutePath()));
    }
}
