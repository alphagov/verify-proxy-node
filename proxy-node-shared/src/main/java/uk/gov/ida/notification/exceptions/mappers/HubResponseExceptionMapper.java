package uk.gov.ida.notification.exceptions.mappers;

import io.dropwizard.jersey.errors.ErrorMessage;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;

public class HubResponseExceptionMapper extends BaseExceptionMapper<HubResponseException> {

    @Override
    protected void handleException(HubResponseException exception) {
        setAuthnRequestValues(
                exception.getSamlResponse().getID(),
                exception.getSamlResponse().getIssuer().getValue(),
                exception.getSamlResponse().getIssueInstant());
    }

    @Override
    protected Response getResponse(HubResponseException exception) {
        String message = MessageFormat.format("Error handling hub response. logId: {0}", getLogId());

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(
                        new ErrorMessage(
                                Response.Status.BAD_REQUEST.getStatusCode(),
                                message
                        ))
                .build();
    }
}
