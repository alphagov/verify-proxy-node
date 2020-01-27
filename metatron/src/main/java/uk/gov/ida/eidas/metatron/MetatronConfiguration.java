package uk.gov.ida.eidas.metatron;

import io.dropwizard.Configuration;

public class MetatronConfiguration extends Configuration {

    private String countriesConfig;

    public String getCountriesConfig() {
        return countriesConfig;
    }
}
