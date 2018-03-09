package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import keystore.KeyStoreResource;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.apprule.rules.EidasProxyNodeAppRule;
import uk.gov.ida.notification.apprule.rules.MetadataClientRule;
import uk.gov.ida.saml.core.test.TestEntityIds;

import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class ProxyNodeAppRuleTestBase {
    @ClassRule
    public static final MetadataClientRule metadataClientRule;

    private static final KeyStoreResource truststore = aKeyStoreResource()
            .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
            .build();

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
            metadataClientRule = new MetadataClientRule();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        truststore.create();
    }

    @Rule
    public EidasProxyNodeAppRule proxyNodeAppRule = new EidasProxyNodeAppRule(
            ConfigOverride.config("proxyNodeEntityId", "http://proxy-node.uk"),
            ConfigOverride.config("proxyNodeResponseUrl", "http://proxy-node/SAML2/SSO/Response"),
            ConfigOverride.config("proxyNodeMetadataForConnectorNodeUrl", "http://proxy-node.uk"),
            ConfigOverride.config("hubUrl", "http://hub"),
            ConfigOverride.config("connectorNodeUrl", "http://connector-node:8080"),
            ConfigOverride.config("connectorNodeIssuerId", "http://connector-node:8080/ConnectorMetadata"),
            ConfigOverride.config("connectorMetadataConfiguration.url", metadataClientRule.baseUri() + "/connector-node/metadata"),
            ConfigOverride.config("connectorMetadataConfiguration.expectedEntityId", "http://connector-node:8080/ConnectorResponderMetadata"),
            ConfigOverride.config("connectorMetadataConfiguration.trustStore.type", "file"),
            ConfigOverride.config("connectorMetadataConfiguration.trustStore.store", truststore.getAbsolutePath()),
            ConfigOverride.config("connectorMetadataConfiguration.trustStore.password", truststore.getPassword()),
            ConfigOverride.config("hubMetadataConfiguration.url", metadataClientRule.baseUri() + "/hub/metadata"),
            ConfigOverride.config("hubMetadataConfiguration.expectedEntityId", TestEntityIds.HUB_ENTITY_ID),
            ConfigOverride.config("hubMetadataConfiguration.trustStore.type", "file"),
            ConfigOverride.config("hubMetadataConfiguration.trustStore.store", truststore.getAbsolutePath()),
            ConfigOverride.config("hubMetadataConfiguration.trustStore.password", truststore.getPassword()),
            ConfigOverride.config("connectorFacingSigningKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("connectorFacingSigningKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("connectorFacingSigningKeyPair.privateKey.key", TEST_PRIVATE_KEY),
            ConfigOverride.config("hubFacingSigningKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("hubFacingSigningKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("hubFacingSigningKeyPair.privateKey.key", TEST_PRIVATE_KEY),
            ConfigOverride.config("hubFacingEncryptionKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("hubFacingEncryptionKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("hubFacingEncryptionKeyPair.privateKey.key", TEST_PRIVATE_KEY)
    );
}
