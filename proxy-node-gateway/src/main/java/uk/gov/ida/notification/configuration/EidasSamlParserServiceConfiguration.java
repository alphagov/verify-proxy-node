package uk.gov.ida.notification.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.proxy.EidasSamlParserProxy;
import uk.gov.ida.notification.shared.IstioHeaderStorage;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.proxy.ProxyNodeJsonClient;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class EidasSamlParserServiceConfiguration extends Configuration {
    @JsonProperty
    @Valid
    @NotNull
    private URI url;

    @JsonProperty
    @Valid
    private JerseyClientConfiguration clientConfig = new JerseyClientConfiguration();

    public URI getURL() {
        return url;
    }

    public JerseyClientConfiguration getClientConfig() {
        return clientConfig;
    }

    public EidasSamlParserProxy buildEidasSamlParserService(Environment environment) {
        Client client = new JerseyClientBuilder(environment).using(clientConfig).build("eidas-saml-parser");
        ProxyNodeJsonClient jsonClient = new ProxyNodeJsonClient(
            new ErrorHandlingClient(client),
            new JsonResponseProcessor(environment.getObjectMapper()),
            new IstioHeaderStorage()
        );

        return new EidasSamlParserProxy(
            jsonClient,
            UriBuilder.fromUri(url).path(Urls.EidasSamlParserUrls.EIDAS_AUTHN_REQUEST_PATH).build()
        );
    }
}
