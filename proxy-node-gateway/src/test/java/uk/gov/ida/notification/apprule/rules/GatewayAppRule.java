package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import org.glassfish.jersey.client.ClientProperties;
import uk.gov.ida.notification.GatewayApplication;
import uk.gov.ida.notification.GatewayConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GatewayAppRule extends AppRule<GatewayConfiguration> {

    public static final String ERROR_PAGE_REDIRECT_URL = "https://proxy-node-error-page";

    private Client noRedirectClient;

    public GatewayAppRule(ConfigOverride... configOverrides) {
        super(
                GatewayApplication.class,
                getConfigOverrides(configOverrides)
        );
    }

    private static ConfigOverride[] getConfigOverrides(ConfigOverride... configOverrides) {
        List<ConfigOverride> configOverridesList = new ArrayList<>();
        configOverridesList.add(ConfigOverride.config("server.applicationConnectors[0].port", "0"));
        configOverridesList.add(ConfigOverride.config("server.adminConnectors[0].port", "0"));
        configOverridesList.add(ConfigOverride.config("server.adminConnectors[0].port", "0"));
        configOverridesList.add(ConfigOverride.config("logging.appenders[0].type", "console"));
        configOverridesList.add(ConfigOverride.config("errorPageRedirectUrl", ERROR_PAGE_REDIRECT_URL));
        configOverridesList.add(ConfigOverride.config("metadataPublishingConfiguration.metadataFilePath", ""));
        configOverridesList.add(ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", ""));
        configOverridesList.add(ConfigOverride.config("redisService.url", ""));
        configOverridesList.addAll(Arrays.asList(configOverrides));
        return configOverridesList.toArray(new ConfigOverride[0]);
    }

    @Override
    public WebTarget target(String path) throws URISyntaxException {
        return target(path, getLocalPort());
    }

    public WebTarget target(String path, int port) throws URISyntaxException {
        if (client == null) {
            client = buildClient();
        }

        return target(client, path, port);
    }

    public WebTarget target(String path, boolean followRedirects) throws URISyntaxException {
        if (!followRedirects) {
            return target(path);
        }

        if (noRedirectClient == null) {
            noRedirectClient = buildClient(false, "test client - no redirects");
        }

        return target(noRedirectClient, path, getLocalPort());
    }

    private WebTarget target(Client client, String path, int port) throws URISyntaxException {
        return client.target(new URI("http://localhost:" + port).resolve(path));
    }

    private Client buildClient() {
        return buildClient(true, "test client");
    }

    private Client buildClient(boolean followRedirects, String clientName) {
        final Client client = new JerseyClientBuilder(getEnvironment())
                .withProperty(ClientProperties.CONNECT_TIMEOUT, 10000)
                .withProperty(ClientProperties.READ_TIMEOUT, 10000)
                .build(clientName);

        if (followRedirects) {
            client.property(ClientProperties.FOLLOW_REDIRECTS, false);
        }

        return client;
    }
}
