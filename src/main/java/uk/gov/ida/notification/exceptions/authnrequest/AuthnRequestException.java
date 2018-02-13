package uk.gov.ida.notification.exceptions.authnrequest;

import org.opensaml.saml.saml2.core.AuthnRequest;

import javax.ws.rs.WebApplicationException;

public class AuthnRequestException extends WebApplicationException {
    private final AuthnRequest authnRequest;

    public AuthnRequestException(Throwable cause, AuthnRequest authnRequest) {
        super(cause);
        this.authnRequest = authnRequest;
    }

    public AuthnRequest getAuthnRequest() {
        return authnRequest;
    }

}
