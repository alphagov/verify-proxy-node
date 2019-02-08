package uk.gov.ida.notification.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import uk.gov.ida.notification.services.EidasSamlParserService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class EidasSamlParserServiceConfiguration extends Configuration {
    @JsonProperty
    @Valid
    @NotNull
    private URI url;

    @JsonProperty
    @Valid
    private JerseyClientConfiguration client = new JerseyClientConfiguration();

    public URI getUrl() {
        return url;
    }

    public JerseyClientConfiguration getClient() {
        return client;
    }

    public EidasSamlParserService buildEidasSamlParserService(Environment environment) {
        return new EidasSamlParserService(
            new JerseyClientBuilder(environment).using(client).build("eidasSamlParser"),
            url.toString()
        );
    }
}
