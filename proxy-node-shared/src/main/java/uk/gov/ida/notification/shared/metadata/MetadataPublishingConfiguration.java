package uk.gov.ida.notification.shared.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class MetadataPublishingConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private URI metadataFilePath;

    @Valid
    @NotNull
    @JsonProperty
    private URI metadataPublishPath;

    URI getMetadataFilePath() {
        return metadataFilePath;
    }

    URI getMetadataPublishPath() {
        return metadataPublishPath;
    }
}
