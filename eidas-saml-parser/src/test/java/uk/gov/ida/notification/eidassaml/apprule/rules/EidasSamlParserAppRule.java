package uk.gov.ida.notification.eidassaml.apprule.rules;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.ClientProperties;
import uk.gov.ida.notification.eidassaml.EidasSamlApplication;
import uk.gov.ida.notification.eidassaml.EidasSamlParserConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class EidasSamlParserAppRule extends DropwizardAppRule<EidasSamlParserConfiguration> {

    private Client client;

    protected EidasSamlParserAppRule(ConfigOverride... configOverrides) {
        super(
                EidasSamlApplication.class,
                resourceFilePath("config.yml"),
                getConfigOverrides(configOverrides)
        );
    }

    private static ConfigOverride[] getConfigOverrides(ConfigOverride... configOverrides) {
        List<ConfigOverride> configOverridesList = new ArrayList<>(Arrays.asList(configOverrides));
        configOverridesList.add(ConfigOverride.config("server.applicationConnectors[0].port", "0"));
        configOverridesList.add(ConfigOverride.config("server.adminConnectors[0].port", "0"));
        configOverridesList.add(ConfigOverride.config("server.adminConnectors[0].port", "0"));
        configOverridesList.add(ConfigOverride.config("logging.appenders[0].type", "console"));
        configOverridesList.add(ConfigOverride.config("replayChecker.redisUrl", ""));
        return configOverridesList.toArray(new ConfigOverride[0]);
    }

    public WebTarget target(String path) throws URISyntaxException {
        return target(path, getLocalPort());
    }

    public WebTarget target(String path, int port) throws URISyntaxException {
        if (client == null) {
            client = new JerseyClientBuilder(getEnvironment())
                    .withProperty(ClientProperties.CONNECT_TIMEOUT, 10000)
                    .withProperty(ClientProperties.READ_TIMEOUT, 10000)
                    .build("test client");
        }

        return client.target(new URI("http://localhost:" + port).resolve(path));
    }
}
