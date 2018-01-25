package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Rule;
import uk.gov.ida.notification.apprule.rules.ConnectorNodeMetadataClientRule;
import uk.gov.ida.notification.apprule.rules.EidasProxyNodeAppRule;
import uk.gov.ida.notification.apprule.rules.HubMetadataClientRule;

public class ProxyNodeAppRuleTestBase {

    @ClassRule
    public static final DropwizardClientRule connectorNodeMetadata = new ConnectorNodeMetadataClientRule();
    public static final DropwizardClientRule hubMetadata = new HubMetadataClientRule();

    @Rule
    public EidasProxyNodeAppRule proxyNodeAppRule = new EidasProxyNodeAppRule(
            ConfigOverride.config("connectorNodeMetadataUrl", connectorNodeMetadata.baseUri() + "/connector-node/metadata"),
            ConfigOverride.config("connectorNodeEntityId", "http://connector-node:8080/ConnectorResponderMetadata"),
            ConfigOverride.config("hubMetadataUrl", hubMetadata.baseUri() + "/hub/metadata"),
            ConfigOverride.config("hubEntityId", "http://hub:8080/HubResponderMetadata")
    );
}
