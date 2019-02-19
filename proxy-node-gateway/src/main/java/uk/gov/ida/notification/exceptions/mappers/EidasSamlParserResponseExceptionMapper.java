package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.exceptions.EidasSamlParserResponseException;
import uk.gov.ida.notification.views.ErrorPageView;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Logger;

public class EidasSamlParserResponseExceptionMapper implements ExceptionMapper<EidasSamlParserResponseException> {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public Response toResponse(EidasSamlParserResponseException exception) {
        log.warning(
            String.format(
                "Exception calling eidas-saml-parser for session '%s': %s",
                exception.getSessionId(),
                exception.getCause().getMessage()
            )
        );

        ApplicationException cause = (ApplicationException) exception.getCause();
        Response.Status status = cause.getExceptionType() == ExceptionType.CLIENT_ERROR ?
            Response.Status.BAD_REQUEST : Response.Status.INTERNAL_SERVER_ERROR;

        return Response.status(status).entity(new ErrorPageView("Something went wrong with the ESP")).build();
    }
}
