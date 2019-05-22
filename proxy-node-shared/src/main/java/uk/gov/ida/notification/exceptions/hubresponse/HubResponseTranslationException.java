package uk.gov.ida.notification.exceptions.hubresponse;

import uk.gov.ida.notification.exceptions.mappers.BaseJsonErrorResponseRuntimeException;

import javax.ws.rs.core.Response;

public class HubResponseTranslationException extends BaseJsonErrorResponseRuntimeException {

    public HubResponseTranslationException(String message) {
        super("Error whilst handling Hub response. " + message);
    }

    @Override
    public Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }
}
