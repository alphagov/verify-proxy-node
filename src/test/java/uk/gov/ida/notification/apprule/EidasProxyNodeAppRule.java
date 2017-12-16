package uk.gov.ida.notification.apprule;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import uk.gov.ida.notification.EidasProxyNodeApplication;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import java.net.URI;
import java.net.URISyntaxException;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class EidasProxyNodeAppRule extends DropwizardAppRule<EidasProxyNodeConfiguration> {
    private Client client;

    public EidasProxyNodeAppRule() {
        super(
                EidasProxyNodeApplication.class,
                resourceFilePath("config.yml"),
                ConfigOverride.config("server.applicationConnectors[0].port", "0"),
                ConfigOverride.config("server.adminConnectors[0].port", "0"),
                ConfigOverride.config("hubFacingSigningKeyPair.publicKey.certFile", "out/production/resources/local/hub_signing_primary.crt"),
                ConfigOverride.config("hubFacingSigningKeyPair.privateKey.keyFile", "out/production/resources/local/hub_signing_primary.pk8")
        );
    }

    public WebTarget target(String path) throws URISyntaxException {
        if (client == null) {
            client = new JerseyClientBuilder(getEnvironment()).build("test client");
        }
        return client.target(new URI("http://localhost:" + getLocalPort()).resolve(path));
    }
}
