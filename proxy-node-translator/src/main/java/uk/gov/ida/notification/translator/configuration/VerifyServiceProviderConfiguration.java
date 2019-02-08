package uk.gov.ida.notification.translator.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class VerifyServiceProviderConfiguration extends Configuration {

    @JsonProperty
    @Valid
    @NotNull
    private URI Url;

    @JsonProperty
    @Valid
    private JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();

    public URI getUrl() {
        return Url;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClientConfiguration;
    }
}