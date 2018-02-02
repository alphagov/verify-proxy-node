package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import keystore.KeyStoreResource;
import org.junit.ClassRule;
import org.junit.Rule;
import uk.gov.ida.notification.apprule.rules.EidasProxyNodeAppRule;
import uk.gov.ida.notification.apprule.rules.MetadataClientRule;

import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class ProxyNodeAppRuleTestBase {
    @ClassRule
    public static final MetadataClientRule metadataClientRule = new MetadataClientRule();

    private static final KeyStoreResource hubTruststore = aKeyStoreResource()
            .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
            .build();

    static {
        hubTruststore.create();
    }

    @Rule
    public EidasProxyNodeAppRule proxyNodeAppRule = new EidasProxyNodeAppRule(
            ConfigOverride.config("connectorNodeMetadataUrl", metadataClientRule.baseUri() + "/connector-node/metadata"),
            ConfigOverride.config("connectorNodeEntityId", "http://connector-node:8080/ConnectorResponderMetadata"),

            ConfigOverride.config("connectorMetadataConfiguration.url", metadataClientRule.baseUri() + "/connector-node/metadata"),
            ConfigOverride.config("connectorMetadataConfiguration.expectedEntityId", "http://connector-node:8080/ConnectorResponderMetadata"),
            ConfigOverride.config("hubMetadataConfiguration.url", metadataClientRule.baseUri() + "/hub/metadata"),
            ConfigOverride.config("hubMetadataConfiguration.expectedEntityId", "http://hub:8080/HubResponderMetadata"),
            ConfigOverride.config("hubMetadataConfiguration.trustStore.path", hubTruststore.getAbsolutePath()),
            ConfigOverride.config("hubMetadataConfiguration.trustStore.password", hubTruststore.getPassword())
    );
}
