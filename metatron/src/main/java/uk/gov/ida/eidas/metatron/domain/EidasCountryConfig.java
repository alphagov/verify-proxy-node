package uk.gov.ida.eidas.metatron.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.security.KeyStore;
import java.util.Optional;

public class EidasCountryConfig {

    @JsonProperty
    @Valid
    @NotNull
    private String name;
    @JsonProperty
    @Valid
    @NotNull
    private String countryCode;
    @JsonProperty
    @Valid
    @NotNull
    private URI connectorMetadata;
    @JsonProperty
    @Valid
    @NotNull
    private boolean enabled;
    @JsonProperty
    @NotNull
    private KeyStore metadataTruststore;
    @JsonProperty
    private KeyStore tlsTruststore;

    public String getName() {
        return name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    @JsonIgnore
    public String getEntityId() {
        return connectorMetadata.toString();
    }

    public URI getConnectorMetadata() {
        return connectorMetadata;
    }

    public KeyStore getMetadataTruststore() {
        return this.metadataTruststore;
    }

    public Optional<KeyStore> getTlsTruststore() {
        return Optional.ofNullable(this.tlsTruststore);
    }
}
