package uk.gov.ida.notification.integration;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import uk.gov.ida.notification.EidasProxyNodeApplication;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class ProxyNodeTestSupport extends DropwizardTestSupport<EidasProxyNodeConfiguration> {
    public ProxyNodeTestSupport(MockHub mockHub) {
        super(
                EidasProxyNodeApplication.class,
                resourceFilePath("config.yml"),
                ConfigOverride.config("hubUrl", mockHub::getHubUrl)
        );
    }
}
