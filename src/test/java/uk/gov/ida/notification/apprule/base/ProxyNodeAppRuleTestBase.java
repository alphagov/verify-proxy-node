package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import org.junit.ClassRule;
import org.junit.Rule;
import uk.gov.ida.notification.apprule.rules.EidasProxyNodeAppRule;
import uk.gov.ida.notification.apprule.rules.MetadataClientRule;

public class ProxyNodeAppRuleTestBase {
    @ClassRule
    public static final MetadataClientRule metadataClientRule = new MetadataClientRule();

    @Rule
    public EidasProxyNodeAppRule proxyNodeAppRule = new EidasProxyNodeAppRule(
            ConfigOverride.config("connectorNodeMetadataUrl", metadataClientRule.baseUri() + "/connector-node/metadata"),
            ConfigOverride.config("connectorNodeEntityId", "http://connector-node:8080/ConnectorResponderMetadata"),
            ConfigOverride.config("hubMetadataUrl", metadataClientRule.baseUri() + "/hub/metadata"),
            ConfigOverride.config("hubEntityId", "http://hub:8080/HubResponderMetadata")
    );
}
