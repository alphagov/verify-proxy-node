package uk.gov.ida.notification.shared.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Optional;

public class MetadataPublishingConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private URI metadataFilePath;

    @Valid
    @NotNull
    @JsonProperty
    private URI metadataPublishPath;

    @Valid
    @JsonProperty
    private URI metadataSigningCertFilePath;

    @Valid
    @NotNull
    @JsonProperty
    private URI metadataCACertsFilePath;

    @Valid
    @NotNull
    @JsonProperty
    private URI metadataCertsPublishPath;

    URI getMetadataFilePath() {
        return metadataFilePath;
    }

    URI getMetadataPublishPath() {
        return metadataPublishPath;
    }

    public URI getMetadataSigningCertFilePath() {
        return Optional.ofNullable(metadataSigningCertFilePath).orElse(URI.create(""));
    }

    public URI getMetadataCACertsFilePath() {
        return metadataCACertsFilePath;
    }

    public URI getMetadataCertsPublishPath() {
        return metadataCertsPublishPath;
    }
}
