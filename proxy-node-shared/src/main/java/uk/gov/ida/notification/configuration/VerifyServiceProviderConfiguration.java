package uk.gov.ida.notification.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import java.net.URI;

public class VerifyServiceProviderConfiguration extends Configuration {

    @JsonProperty
    @Valid
    @NotNull
    private URI url;

    @JsonProperty
    @Valid
    private JerseyClientConfiguration clientConfig = new JerseyClientConfiguration();

    public URI getUrl() {
        return url;
    }

    public JerseyClientConfiguration getClientConfiguration() {
        return clientConfig;
    }

    public VerifyServiceProviderProxy buildVerifyServiceProviderProxy(Environment environment) {
        Client client = new JerseyClientBuilder(environment).using(clientConfig).build("vsp-client");
        JsonClient jsonClient = new JsonClient(
            new ErrorHandlingClient(client),
            new JsonResponseProcessor(environment.getObjectMapper())
        );

        return new VerifyServiceProviderProxy(jsonClient, url);
    }
}
