package uk.gov.ida.eidas.metatron.apprule.rules;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.ClientProperties;
import uk.gov.ida.eidas.metatron.MetatronApplication;
import uk.gov.ida.eidas.metatron.MetatronConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class MetatronAppRule extends DropwizardAppRule<MetatronConfiguration> {
    private Client client;

    public MetatronAppRule(ConfigOverride... configOverrides) {
        super(
                MetatronApplication.class,
                resourceFilePath("config.yml"),
                getConfigOverrides(configOverrides)
        );
    }

    private static ConfigOverride[] getConfigOverrides(ConfigOverride... configOverrides) {
        List<ConfigOverride> overrides = new ArrayList<>(Arrays.asList(configOverrides));
        overrides.add(ConfigOverride.config("server.applicationConnectors[0].port", "0"));
        overrides.add(ConfigOverride.config("server.adminConnectors[0].port", "0"));
        overrides.add(ConfigOverride.config("server.adminConnectors[0].port", "0"));
        overrides.add(ConfigOverride.config("logging.appenders[0].type", "console"));
        return overrides.toArray(new ConfigOverride[0]);
    }

    public WebTarget target(String path) throws URISyntaxException {
        return target(path, getLocalPort());
    }

    private WebTarget target(String path, int port) throws URISyntaxException {
        if (client == null) {
            client = new JerseyClientBuilder(getEnvironment())
                    .withProperty(ClientProperties.CONNECT_TIMEOUT, 10000)
                    .withProperty(ClientProperties.READ_TIMEOUT, 10000)
                    .build("test client");
        }

        return client.target(new URI("http://localhost:" + port).resolve(path));
    }
}
