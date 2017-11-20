package uk.gov.ida.notification.saml.translation;

public class EidasAuthnRequestException extends RuntimeException {
    EidasAuthnRequestException() {
        super("Bad SAML AuthnRequest from Connector");
    }
    EidasAuthnRequestException(String message) {
        super("Bad SAML AuthnRequest from Connector: " + message);
    }
}
