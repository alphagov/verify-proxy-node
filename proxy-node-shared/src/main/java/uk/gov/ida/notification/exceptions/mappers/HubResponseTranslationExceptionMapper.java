package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;

import javax.ws.rs.core.Response;

import static java.text.MessageFormat.format;

public class HubResponseTranslationExceptionMapper extends BaseExceptionMapper<HubResponseTranslationException> {

    @Override
    protected void handleException(HubResponseTranslationException exception) { }

    @Override
    protected Response getResponse(HubResponseTranslationException exception) {
        final String message = format("Error whilst handling hub response: {0}; {1}", exception.getMessage(), exception.getCause().getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }
}
