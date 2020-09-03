package uk.gov.ida.eidas.metatron;

import engineering.reliability.gds.metrics.config.PrometheusConfiguration;
import io.dropwizard.Configuration;

public class MetatronConfiguration extends Configuration implements PrometheusConfiguration {

    private String countriesConfig;

    public String getCountriesConfig() {
        return countriesConfig;
    }

    @Override
    public boolean isPrometheusEnabled() {
        return true;
    }
}
