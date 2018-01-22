package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import uk.gov.ida.notification.EidasProxyNodeApplication;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class EidasProxyNodeAppRule extends DropwizardAppRule<EidasProxyNodeConfiguration> {
    private Client client;

    public EidasProxyNodeAppRule(ConfigOverride... configOverrides) {
        super(
            EidasProxyNodeApplication.class,
            resourceFilePath("config.yml"),
            getConfigOverrides(configOverrides)
        );
    }

    private static ConfigOverride[] getConfigOverrides(ConfigOverride... configOverrides) {
        List<ConfigOverride> configOverridesList = new ArrayList<>(Arrays.asList(configOverrides));
        configOverridesList.add(ConfigOverride.config("server.applicationConnectors[0].port", "0"));
        configOverridesList.add(ConfigOverride.config("server.adminConnectors[0].port", "0"));
        configOverridesList.add(ConfigOverride.config("server.adminConnectors[0].port", "0"));
        configOverridesList.add(ConfigOverride.config("httpClient.timeout", "25s"));
        return configOverridesList.toArray(new ConfigOverride[0]);
    }

    public WebTarget target(String path) throws URISyntaxException {
        if (client == null) {
            client = new JerseyClientBuilder(getEnvironment())
                    .build("test client");
        }
        return client.target(new URI("http://localhost:" + getLocalPort()).resolve(path));
    }
}