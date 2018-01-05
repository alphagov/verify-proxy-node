package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Rule;
import uk.gov.ida.notification.apprule.rules.ConnectorNodeMetadataClientRule;
import uk.gov.ida.notification.apprule.rules.EidasProxyNodeAppRule;

public class ProxyNodeAppRuleTestBase {

    @ClassRule
    public static final DropwizardClientRule connectorNodeMetadata = new ConnectorNodeMetadataClientRule();

    @Rule
    public EidasProxyNodeAppRule proxyNodeAppRule = new EidasProxyNodeAppRule(
            ConfigOverride.config("connectorNodeMetadataUrl", connectorNodeMetadata.baseUri() + "/connector-node/metadata"),
            ConfigOverride.config("connectorNodeEntityId", "http://connector-node:8080/ConnectorResponderMetadata")
    );
}
