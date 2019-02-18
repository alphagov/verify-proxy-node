package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.proxy.VerifyServiceProviderResponseException;
import uk.gov.ida.notification.views.ErrorPageView;


import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Logger;

public class VerifyServiceProviderResponseExceptionMapper implements ExceptionMapper<VerifyServiceProviderResponseException> {
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public Response toResponse(VerifyServiceProviderResponseException exception) {
        log.warning(String.format("Exception calling verify-service-provider: %s", exception.getCause().getMessage()));

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorPageView("Something went wrong with the VSP")).build();
    }
}
