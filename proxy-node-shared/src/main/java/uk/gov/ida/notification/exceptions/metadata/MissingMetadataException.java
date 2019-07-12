package uk.gov.ida.notification.exceptions.metadata;

public class MissingMetadataException extends RuntimeException {

    public MissingMetadataException(String metadataFileLocation) {
        this(metadataFileLocation, null);
    }

    public MissingMetadataException(String metadataFileLocation, Throwable cause) {
        super("Failed to read metadata from file: " + metadataFileLocation, cause);
    }
}
