package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.exceptions.TranslatorResponseException;
import uk.gov.ida.notification.views.ErrorPageView;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Logger;

public class TranslatorResponseExceptionMapper implements ExceptionMapper<TranslatorResponseException> {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public Response toResponse(TranslatorResponseException exception) {
        log.warning(
            String.format(
                "Exception calling translator for session '%s': %s",
                exception.getSessionId(),
                exception.getCause().getMessage()
            )
        );

        ApplicationException cause = (ApplicationException) exception.getCause();
        Response.Status status = cause.getExceptionType() == ExceptionType.CLIENT_ERROR ?
            Response.Status.BAD_REQUEST : Response.Status.INTERNAL_SERVER_ERROR;

        return Response
            .status(status)
            .entity(new ErrorPageView("Something went wrong with the Translator"))
            .build();
    }

}
