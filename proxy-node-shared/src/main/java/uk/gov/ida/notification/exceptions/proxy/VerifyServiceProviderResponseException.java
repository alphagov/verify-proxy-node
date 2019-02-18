package uk.gov.ida.notification.exceptions.proxy;

import javax.ws.rs.WebApplicationException;

public class VerifyServiceProviderResponseException extends WebApplicationException {

    public VerifyServiceProviderResponseException (Throwable cause) {
        super(cause);
    }
}
