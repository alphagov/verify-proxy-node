package uk.gov.ida.notification.exceptions;

public class SamlSigningException extends RuntimeException {
    public SamlSigningException(Throwable cause) {
        super("Failed to sign SAML object", cause);
    }
}
