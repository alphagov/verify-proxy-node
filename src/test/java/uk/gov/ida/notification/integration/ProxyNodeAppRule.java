package uk.gov.ida.notification.integration;

import io.dropwizard.testing.junit.DropwizardAppRule;
import uk.gov.ida.notification.EidasProxyNodeApplication;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class ProxyNodeAppRule extends DropwizardAppRule<EidasProxyNodeConfiguration> {
    public ProxyNodeAppRule() {
        super(
                EidasProxyNodeApplication.class,
                resourceFilePath("config.yml")
        );
    }
}
