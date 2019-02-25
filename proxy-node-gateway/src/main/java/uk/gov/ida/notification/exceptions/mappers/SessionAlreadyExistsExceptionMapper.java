package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.SessionAlreadyExistsException;
import uk.gov.ida.notification.exceptions.SessionMissingException;
import uk.gov.ida.notification.views.ErrorPageView;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Logger;

public class SessionAlreadyExistsExceptionMapper implements ExceptionMapper<SessionAlreadyExistsException> {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public Response toResponse(SessionAlreadyExistsException exception) {
        log.warning(exception.getMessage());

        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(new ErrorPageView("Something went wrong session already exists"))
            .build();
    }
}
