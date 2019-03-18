package uk.gov.ida.notification.exceptions.mappers;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.notification.views.ErrorPageView;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.text.MessageFormat.format;

public abstract class ExceptionToErrorPageMapper<TException extends Exception> implements ExceptionMapper<TException> {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionToErrorPageMapper.class);

    private UriInfo uriInfo;
    private String logId;

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public Response toResponse(TException exception) {
        this.logId = String.format("%016x", ThreadLocalRandom.current().nextLong());

        logException(exception);

        return Response.status(getResponseStatus(exception))
                .entity(new ErrorPageView(getErrorPageMessage(exception)))
                .build();
    }

    protected abstract Response.Status getResponseStatus(TException exception);

    protected abstract String getErrorPageMessage(TException exception);

    String getLogId() {
        return logId;
    }

    // TODO: Log these three properties via the logger itself as context attributes
    protected String getAuthnRequestId(TException exception) {
        return null;
    }

    protected String getIssuerId(TException exception) {
        return null;
    }

    protected String getSessionId(TException exception) {
        return null;
    }

    protected DateTime getIssueInstant(TException exception) {
        return null;
    }

    private void logException(TException exception) {
        String message = exception.getMessage();
        String cause = Optional.ofNullable(exception.getCause()).map(Throwable::getMessage).orElse(null);

        LOG.error(format("Error whilst contacting uri [{0}]; logId: {1}; requestId: {2}; sessionId: {3}, issuer: {4}; issueInstant: {5}; message: {6}, cause: {7}",
                uriInfo.getPath(), logId, getAuthnRequestId(exception), getSessionId(exception), getIssuerId(exception), getIssueInstant(exception), message, cause),
                exception
        );
    }
}
