package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.proxy.VspGenerateAuthnRequestResponseException;
import uk.gov.ida.notification.views.ErrorPageView;


import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Logger;

public class VspGenerateAuthnRequestResponseExceptionMapper implements ExceptionMapper<VspGenerateAuthnRequestResponseException> {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public Response toResponse(VspGenerateAuthnRequestResponseException exception) {
        log.warning(
                String.format(
                        "Exception calling verify-service-provider for session '%s': %s",
                        exception.getSessionId(),
                        exception.getCause().getMessage()
                )
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorPageView("Something went wrong with the VSP")).build();
    }
}
