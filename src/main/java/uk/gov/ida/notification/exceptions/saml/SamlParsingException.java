package uk.gov.ida.notification.exceptions.saml;

public class SamlParsingException extends RuntimeException {
    public SamlParsingException(Throwable cause) {
        super("Failed to parse XML", cause);
    }
}
