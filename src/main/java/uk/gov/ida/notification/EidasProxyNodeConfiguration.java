package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class EidasProxyNodeConfiguration extends Configuration {

    @JsonProperty
    @Valid
    @NotNull
    private URI hubUrl;

    @JsonProperty
    @Valid
    private JerseyClientConfiguration hubClient = new JerseyClientConfiguration();

    public URI getHubUrl() {
        return hubUrl;
    }

    public JerseyClientConfiguration getHubClient() {
        return hubClient;
    }
}
