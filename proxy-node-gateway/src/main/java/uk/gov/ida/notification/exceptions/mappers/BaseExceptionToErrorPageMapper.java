package uk.gov.ida.notification.exceptions.mappers;

import io.prometheus.client.Counter;
import uk.gov.ida.notification.MetricsUtils;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.net.URI;
import java.util.logging.Level;

import static java.text.MessageFormat.format;

public abstract class BaseExceptionToErrorPageMapper<TException extends Exception> implements ExceptionMapper<TException> {
    // TODO multi-country PN will need a way to distinguish these counters per country
    private static final Counter FAILURE_ERROR_PAGE = Counter.build(
            MetricsUtils.LABEL_PREFIX + "_failure_error_page",
            "Number of failures reported to the user via a 303 redirect to a Verify error page, eg because an error could not be reported to a remote connector via SAML")
            .register();
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
        FAILURE_ERROR_PAGE.inc();
        logException(exception);
        return Response.seeOther(errorPageRedirectUrl).build();
    }

    public abstract Level getLogLevel(TException exception);

    private void logException(TException exception) {
        ProxyNodeLogger.logException(exception, getLogLevel(exception),
                format("Error whilst contacting URI [{0}]", this.uriInfo.getAbsolutePath()));
    }
}
