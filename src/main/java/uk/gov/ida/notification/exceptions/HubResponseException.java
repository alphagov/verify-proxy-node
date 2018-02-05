package uk.gov.ida.notification.exceptions;

import org.opensaml.saml.saml2.core.Response;

import javax.ws.rs.WebApplicationException;

public class HubResponseException extends WebApplicationException {
    private final Response samlResponse;

    public HubResponseException(Throwable cause, Response samlResponse) {
        super(cause);
        this.samlResponse = samlResponse;
    }

    public Response getSamlResponse() {
        return samlResponse;
    }
}
