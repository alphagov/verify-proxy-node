package uk.gov.ida.notification.saml;

public class SamlSigningException extends RuntimeException {
    public SamlSigningException(Throwable cause) {
        super("Failed to sign SAML object", cause);
    }
}
