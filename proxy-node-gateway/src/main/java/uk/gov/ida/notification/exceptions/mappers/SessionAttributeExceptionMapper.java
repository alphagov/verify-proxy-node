package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.SessionAttributeException;
import uk.gov.ida.notification.views.ErrorPageView;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Logger;

public class SessionAttributeExceptionMapper implements ExceptionMapper<SessionAttributeException> {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public Response toResponse(SessionAttributeException exception) {
        log.warning(
            String.format(
                "Exception reading attributes for session '%s': %s",
                exception.getSessionId(),
                exception.getMessage()
            )
        );

        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(new ErrorPageView("Something went wrong with the session attributes"))
            .build();
    }
}
