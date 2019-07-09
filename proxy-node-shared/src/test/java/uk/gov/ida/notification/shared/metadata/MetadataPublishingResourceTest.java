package uk.gov.ida.notification.shared.metadata;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import uk.gov.ida.notification.exceptions.metadata.MissingMetadataException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MetadataPublishingResourceTest {

    private MetadataPublishingResource metadataPublishingResource;

    @Test
    public void shouldReturnExistingMetadataResource() throws URISyntaxException, IOException {
        final String metadataFilePath = "metadata/test-metadata.xml";
        final String expectedMetadata = new String(Files.readAllBytes(Paths.get(
                getClass().getClassLoader().getResource(metadataFilePath).getPath())));

        URI metadataResourcePath = new URI(metadataFilePath);
        metadataPublishingResource = new MetadataPublishingResource(metadataResourcePath);

        final String metadata = IOUtils.toString(
                (BufferedInputStream) metadataPublishingResource.getMetadata().getEntity(), StandardCharsets.UTF_8);

        assertThat(metadata).isEqualTo(expectedMetadata);
    }

    @Test
    public void shouldThrowMetadataMissingExceptionWhenMetadataNotFound() throws URISyntaxException {
        final String metadataFilePath = "metadata/invalid-metadata-path.xml";
        final URI metadataResourcePath = new URI(metadataFilePath);
        metadataPublishingResource = new MetadataPublishingResource(metadataResourcePath);

        assertThatThrownBy(() -> metadataPublishingResource.getMetadata())
                .isInstanceOf(MissingMetadataException.class)
                .hasMessageContaining(metadataFilePath);
    }
}
