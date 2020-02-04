package uk.gov.ida.notification.eidassaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import engineering.reliability.gds.metrics.config.PrometheusConfiguration;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.configuration.ReplayCheckerConfiguration;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.proxy.MetatronProxy;
import uk.gov.ida.notification.shared.proxy.ProxyNodeJsonClient;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import java.net.URI;

public class EidasSamlParserConfiguration extends Configuration implements PrometheusConfiguration {

    @JsonProperty
    @Valid
    @NotNull
    private URI proxyNodeAuthnRequestUrl;

    @JsonProperty
    @Valid
    private final ReplayCheckerConfiguration replayChecker = new ReplayCheckerConfiguration();

    @NotNull
    @JsonProperty
    private URI url;

    @Valid
    @NotNull
    @JsonProperty
    private final JerseyClientConfiguration clientConfig = new JerseyClientConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private URI metatronUri;

    public URI getUrl() {
        return url;
    }

    public URI getMetatronUrl() {
        return metatronUri;
    }

    public JerseyClientConfiguration getClientConfig() {
        return clientConfig;
    }

    public ReplayCheckerConfiguration getReplayChecker() {
        return replayChecker;
    }

    public URI getProxyNodeAuthnRequestUrl() {
        return proxyNodeAuthnRequestUrl;
    }

    @Override
    public boolean isPrometheusEnabled() {
        return true;
    }

    public MetatronProxy buildMetatronProxy(Environment environment) {
        Client client = new JerseyClientBuilder(environment).using(clientConfig).build("metatron-client");
        ProxyNodeJsonClient jsonClient = new ProxyNodeJsonClient(
                new ErrorHandlingClient(client),
                new JsonResponseProcessor(environment.getObjectMapper()),
                new IstioHeaderStorage()
        );
        return new MetatronProxy(jsonClient, metatronUri);
    }
}
