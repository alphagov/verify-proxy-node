package uk.gov.ida.notification.exceptions.metadata;

public class MissingMetadataException extends RuntimeException {

    public MissingMetadataException(String metadataLocation) {
        super("Metadata missing from " + metadataLocation);
    }

    public MissingMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
