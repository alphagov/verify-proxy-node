package uk.gov.ida.eidas.metadataservice.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
    private String connectorMetadata;
    @JsonProperty
    @Valid
    @NotNull
    private boolean enabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getConnectorMetadata() {
        return connectorMetadata;
    }

    public void setConnectorMetadata(String connectorMetadata) {
        this.connectorMetadata = connectorMetadata;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
