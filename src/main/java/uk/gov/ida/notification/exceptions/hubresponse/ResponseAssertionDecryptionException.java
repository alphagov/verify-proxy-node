package uk.gov.ida.notification.exceptions.hubresponse;

public class ResponseAssertionDecryptionException extends RuntimeException {
    public ResponseAssertionDecryptionException(Throwable cause) {
        super("Failed to decrypt assertion", cause);
    }
}
