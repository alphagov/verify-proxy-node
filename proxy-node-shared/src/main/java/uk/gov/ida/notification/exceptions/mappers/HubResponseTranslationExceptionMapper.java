package uk.gov.ida.notification.exceptions.mappers;

import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;

import javax.ws.rs.core.Response;

import static java.text.MessageFormat.format;

public class HubResponseTranslationExceptionMapper extends BaseExceptionMapper<HubResponseTranslationException> {

    @Override
    protected Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }

    @Override
    protected String getResponseMessage(HubResponseTranslationException exception) {
        return format("Error whilst handling hub response: {0}; {1}", exception.getMessage(), exception.getCause().getMessage());
    }
}
