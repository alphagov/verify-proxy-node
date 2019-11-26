package uk.gov.ida.eidas.metadataservice.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.KeyStore;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class EidasConfig {
    @JsonProperty
    private ConcurrentLinkedQueue<EidasCountryConfig> countries;

    @JsonProperty
    private KeyStore keystore;

    public Collection<EidasCountryConfig> getCountries() {
        return countries;
    }

    public KeyStore getKeyStore() {
        return this.keystore;
    }

    public void retainAll(Function<EidasConfig, Collection<EidasCountryConfig>> filterFunction) {
        this.countries.retainAll(filterFunction.apply(this));
    }
}
