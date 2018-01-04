package uk.gov.ida.notification.exceptions;

public class MissingMetadataException extends RuntimeException {
    public MissingMetadataException(String message) {
        super("Element is not available in metadata: " + message);
    }
}
