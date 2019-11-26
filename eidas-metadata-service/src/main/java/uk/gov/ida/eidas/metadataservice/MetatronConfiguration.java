package uk.gov.ida.eidas.metadataservice;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.*;

import uk.gov.ida.eidas.metadataservice.core.dto.EidasConfig;

public class MetatronConfiguration extends Configuration {
    @Valid
    @NotNull
    private EidasConfig eidasConfig;

    @JsonProperty("eidasConfig")
    public EidasConfig getEidasConfig() {
        return this.eidasConfig;
    }

    @JsonProperty("eidasConfig")
    public EidasConfig setEidasConfig(EidasConfig eidasConfig) {
        return this.eidasConfig = eidasConfig;
    }
}
