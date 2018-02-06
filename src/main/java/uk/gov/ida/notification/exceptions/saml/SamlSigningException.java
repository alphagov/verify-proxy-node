package uk.gov.ida.notification.exceptions.saml;

public class SamlSigningException extends RuntimeException {
    public SamlSigningException(Throwable cause) {
        super("Failed to sign SAML object", cause);
    }
}
