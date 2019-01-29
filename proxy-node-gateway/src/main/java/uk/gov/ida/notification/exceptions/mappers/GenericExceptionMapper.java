package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.views.ErrorPageView;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public Response toResponse(Exception exception) {
        String logId = String.format("%016x", ThreadLocalRandom.current().nextLong());

        log.log(
            Level.WARNING,
            String.format(
                "logId=%s, cause=%s",
                logId,
                exception.getCause()
            )
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new ErrorPageView(exception))
            .build();
    }
}
