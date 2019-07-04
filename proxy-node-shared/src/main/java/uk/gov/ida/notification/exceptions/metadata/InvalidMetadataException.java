package uk.gov.ida.notification.exceptions.metadata;

public class InvalidMetadataException extends RuntimeException {

    public InvalidMetadataException(String message) {
        super("Element is not available in metadata: " + message);
    }

    public InvalidMetadataException(String message, Exception innerException) {
        super(message, innerException);
    }
}
