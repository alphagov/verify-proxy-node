package uk.gov.ida.notification.pki;

public class CertificateUnmarshallingException extends RuntimeException {
    public CertificateUnmarshallingException(Throwable cause) {
        super("Failed to unmarshall certificate", cause);
    }
}
