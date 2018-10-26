package uk.gov.ida.notification.exceptions.metadata;

public class MissingMetadataException extends RuntimeException {
    public MissingMetadataException(String message) {
        super("Element is not available in metadata: " + message);
    }
}
