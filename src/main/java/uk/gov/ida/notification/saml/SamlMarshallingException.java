package uk.gov.ida.notification.saml;

public class SamlMarshallingException extends RuntimeException {
    public SamlMarshallingException(Throwable cause) {
        super("Failed to marshallToElement SAML object", cause);
    }
}
