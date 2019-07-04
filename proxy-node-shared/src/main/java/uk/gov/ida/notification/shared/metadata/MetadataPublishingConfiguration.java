package uk.gov.ida.notification.shared.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class MetadataPublishingConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private URI metadataResourceFilePath;

    @Valid
    @NotNull
    @JsonProperty
    private URI metadataPublishPath;

    public URI getMetadataResourceFilePath() {
        return metadataResourceFilePath;
    }

    public URI getMetadataPublishPath() {
        return metadataPublishPath;
    }
}
