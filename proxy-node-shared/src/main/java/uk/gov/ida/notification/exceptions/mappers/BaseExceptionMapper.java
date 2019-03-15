package uk.gov.ida.notification.exceptions.mappers;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.concurrent.ThreadLocalRandom;

import static java.text.MessageFormat.format;

public abstract class BaseExceptionMapper<TException extends Exception> implements ExceptionMapper<TException> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseExceptionMapper.class);

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

        return Response.status(getResponseStatus())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorMessage(
                        getResponseStatus().getStatusCode(),
                        getResponseMessage(exception)))
                .build();
    }

    protected abstract Response.Status getResponseStatus();

    protected abstract String getResponseMessage(TException exception);

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

    protected DateTime getIssueInstant(TException exception) {
        return null;
    }

    private void logException(TException exception) {
        String message = exception.getMessage();
        String logId = String.format("%016x", ThreadLocalRandom.current().nextLong());

        LOG.error(format("Error whilst contacting uri [{0}]; logId: {1}; requestId: {2}; issuer: {3}; issueInstant: {4}; cause: {5}",
                uriInfo.getPath(), logId, getAuthnRequestId(exception), getIssuerId(exception), getIssueInstant(exception), message),
                exception
        );
    }
}
