package uk.gov.ida.notification.exceptions.mappers;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.concurrent.ThreadLocalRandom;

import static java.text.MessageFormat.format;

public abstract class BaseExceptionMapper<TException extends Exception> implements ExceptionMapper<TException> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseExceptionMapper.class);

    private UriInfo uriInfo;
    private HttpServletRequest httpServletRequest;

    private String logId;
    private String authnRequestId;
    private String issuerId;
    private DateTime issueInstant;

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Context
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public Response toResponse(TException exception) {
        this.logId = String.format("%016x", ThreadLocalRandom.current().nextLong());

        handleException(exception);
        logException(exception);

        return getResponse(exception);
    }

    protected abstract void handleException(TException exception);

    protected abstract Response getResponse(TException exception);

    String getLogId() {
        return logId;
    }

    void setAuthnRequestValues(String authnRequestId, String issuerId, DateTime issueInstant) {
        this.authnRequestId = authnRequestId;
        this.issuerId = issuerId;
        this.issueInstant = issueInstant;
    }

    private void logException(TException exception) {
        String message = exception.getMessage();
        String logId = String.format("%016x", ThreadLocalRandom.current().nextLong());

        LOG.warn(format("Error whilst contacting uri [{0}]; logId: {1}; requestId: {2}; issuer: {3}; issueInstant: {4}; cause: {5}",
                uriInfo.getPath(), logId, authnRequestId, issuerId, issueInstant, message),
                exception
        );
    }
}
