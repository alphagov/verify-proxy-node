package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.shared.ProxyNodeLogger;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.net.URI;
import java.util.logging.Level;

import static java.text.MessageFormat.format;

public abstract class ExceptionToErrorPageMapper<TException extends Exception> implements ExceptionMapper<TException> {

    private final ProxyNodeLogger proxyNodeLogger = new ProxyNodeLogger();

    private final URI errorPageRedirectUrl;

    private UriInfo uriInfo;

    ExceptionToErrorPageMapper(URI errorPageRedirectUrl) {
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

    private void logException(TException exception) {
        proxyNodeLogger.addContext(exception);
        // This error level will be addressed in the next PR/Story concerning ExceptionToSamlErrorResponseMapper
        proxyNodeLogger.log(Level.WARNING, format("Error whilst contacting uri [{0}]", this.uriInfo.getPath()));
    }
}
