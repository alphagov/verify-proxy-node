package uk.gov.ida.notification.shared.metadata;

import uk.gov.ida.notification.exceptions.metadata.MissingMetadataException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

public class MetadataPublishingResource {

    private final String metadataFilePath;

    @Inject
    public MetadataPublishingResource(@Named("metadataFilePath") URI metadataFilePath) {
        this.metadataFilePath = metadataFilePath.toString();
    }

    @GET
    public Response getMetadata() {

        final File metadataFile = new File(metadataFilePath);
        if (!metadataFile.exists()) {
            throw new MissingMetadataException(metadataFilePath);
        }

        final InputStream metadataFileInputStream;
        try {
            metadataFileInputStream = new FileInputStream(metadataFile);
        } catch (FileNotFoundException e) {
            throw new MissingMetadataException(metadataFilePath, e);
        }

        return Response.ok(metadataFileInputStream, MediaType.APPLICATION_XML).build();
    }
}
