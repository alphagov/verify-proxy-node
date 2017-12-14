package uk.gov.ida.notification.exceptions;

public class EidasAuthnRequestException extends RuntimeException {
    public EidasAuthnRequestException(String message) {
        super("Bad SAML AuthnRequest from Connector: " + message);
    }
}
