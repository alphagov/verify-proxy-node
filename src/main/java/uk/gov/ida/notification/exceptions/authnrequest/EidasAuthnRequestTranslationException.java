package uk.gov.ida.notification.exceptions.authnrequest;

public class EidasAuthnRequestTranslationException extends RuntimeException {
    public EidasAuthnRequestTranslationException(String message) {
        super("Bad SAML AuthnRequest from Connector: " + message);
    }
}
