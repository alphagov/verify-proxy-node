package uk.gov.ida.notification.exceptions.hubresponse;

public class ResponseAssertionEncryptionException extends RuntimeException {
    public ResponseAssertionEncryptionException(Throwable cause) {
        super("Failed to encrypt assertion", cause);
    }
}
