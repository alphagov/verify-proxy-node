package uk.gov.ida.notification.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.proxy.TranslatorProxy;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static uk.gov.ida.notification.shared.Urls.TranslatorUrls.TRANSLATOR_ROOT;

public class TranslatorServiceConfiguration extends Configuration {

    @NotNull
    @JsonProperty
    private URI url;

    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration clientConfig = new JerseyClientConfiguration();

    public URI getUrl() {
        return url;
    }

    public JerseyClientConfiguration getClientConfig() {
        return clientConfig;
    }

    public TranslatorProxy buildTranslatorProxy(Environment environment) {
        Client client = new JerseyClientBuilder(environment).using(clientConfig).build("translator-client");
        JsonClient jsonClient = new JsonClient(
            new ErrorHandlingClient(client),
            new JsonResponseProcessor(environment.getObjectMapper())
        );
        return new TranslatorProxy(
            jsonClient,
            UriBuilder.fromUri(url).path(TRANSLATOR_ROOT).build()
        );
    }
}
