package uk.gov.ida.notification.exceptions.saml;

public class SamlUnmarshallingException extends RuntimeException {
    public SamlUnmarshallingException(Throwable cause) {
        super("Failed to unmarshall To SAML object", cause);
    }
}
