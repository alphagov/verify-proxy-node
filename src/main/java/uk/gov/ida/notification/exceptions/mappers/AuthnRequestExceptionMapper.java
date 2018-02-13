package uk.gov.ida.notification.exceptions.mappers;

import io.dropwizard.jersey.errors.ErrorMessage;
import uk.gov.ida.notification.exceptions.authnrequest.AuthnRequestException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthnRequestExceptionMapper implements ExceptionMapper<AuthnRequestException> {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public Response toResponse(AuthnRequestException exception) {
        String message = exception.getCause().getMessage();
        String logId = String.format("%016x", ThreadLocalRandom.current().nextLong());

        log.log(Level.WARNING, String.format("logId=%s, requestId=%s, issuer=%s, issueInstant=%s, cause=%s",
                logId,
                exception.getAuthnRequest().getID(),
                exception.getAuthnRequest().getIssuer().getValue(),
                exception.getAuthnRequest().getIssueInstant(),
                message)
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), "Error handling authn request. logId: " + logId))
                .build();
    }

}
