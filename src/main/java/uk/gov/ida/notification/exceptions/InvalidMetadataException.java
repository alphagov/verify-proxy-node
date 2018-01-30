package uk.gov.ida.notification.exceptions;

public class InvalidMetadataException extends RuntimeException {
    public InvalidMetadataException(String message, Exception innerException) {
        super(message);
        innerException.printStackTrace();
    }
}
