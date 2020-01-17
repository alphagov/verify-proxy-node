package uk.gov.ida.eidas.metatron.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EidasConfig {
    @JsonProperty
    private List<EidasCountryConfig> countries = new ArrayList<>();

    public Collection<EidasCountryConfig> getCountries() {
        return countries;
    }
}
