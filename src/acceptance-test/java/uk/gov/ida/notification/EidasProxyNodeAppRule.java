package uk.gov.ida.notification;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class EidasProxyNodeAppRule extends DropwizardAppRule<EidasProxyNodeConfiguration> {
    public EidasProxyNodeAppRule() {
        super(
                EidasProxyNodeApplication.class,
                resourceFilePath("config.yml"),
                ConfigOverride.config("server.applicationConnectors[0].port", "0"),
                ConfigOverride.config("server.adminConnectors[0].port", "0")
        );
    }
}
