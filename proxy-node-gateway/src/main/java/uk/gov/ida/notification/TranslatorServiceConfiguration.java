package uk.gov.ida.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import uk.gov.ida.notification.saml.SamlParser;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class TranslatorServiceConfiguration extends Configuration {
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

    public TranslatorService buildTranslatorService(Environment environment, SamlParser samlParser) {
        return new TranslatorService(
            new JerseyClientBuilder(environment).using(client).build("translator"),
            url.toString(),
            samlParser
        );
    }
}
