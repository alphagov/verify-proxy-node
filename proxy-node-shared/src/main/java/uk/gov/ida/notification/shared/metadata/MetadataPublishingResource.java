package uk.gov.ida.notification.shared.metadata;

import uk.gov.ida.notification.exceptions.metadata.MissingMetadataException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

public class MetadataPublishingResource {

    private final String metadataResourcePath;

    @Inject
    public MetadataPublishingResource(@Named("metadataResourceFilePath") URI metadataResourcePath) {
        this.metadataResourcePath = metadataResourcePath.toString();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getMetadata() {
        final Optional<File> metadataResourceFile = Optional.of(getClass())
                .map(Class::getClassLoader)
                .map(l -> l.getResource(metadataResourcePath))
                .map(URL::getPath)
                .map(File::new);

        if (metadataResourceFile.isEmpty() || !metadataResourceFile.get().exists()) {
            throw new MissingMetadataException(metadataResourcePath);
        }

        final InputStream metadataResource = getClass().getClassLoader().getResourceAsStream(metadataResourcePath);
        return Response.ok(metadataResource, MediaType.APPLICATION_XML).build();
    }
}
