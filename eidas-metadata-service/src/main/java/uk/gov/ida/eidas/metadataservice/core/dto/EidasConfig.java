package uk.gov.ida.eidas.metadataservice.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class EidasConfig {
    @JsonProperty
    ConcurrentLinkedQueue<EidasCountryConfig> countries;

    public Collection<EidasCountryConfig> getCountries() {
        return countries;
    }

    public void retainAll(Function<EidasConfig, Collection<EidasCountryConfig>> filterFunction) {
        this.countries.retainAll(filterFunction.apply(this));
    }
}
