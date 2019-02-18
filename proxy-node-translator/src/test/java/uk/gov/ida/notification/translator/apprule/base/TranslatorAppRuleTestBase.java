package uk.gov.ida.notification.translator.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import keystore.KeyStoreResource;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.apprule.rules.MetadataClientRule;
import uk.gov.ida.notification.translator.apprule.rules.TranslatorAppRule;
import uk.gov.ida.notification.translator.apprule.rules.VspClientRule;

import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class TranslatorAppRuleTestBase {

    @ClassRule
    public static final MetadataClientRule metadataClientRule;

    @ClassRule
    public static final VspClientRule vspClientRule;

    private static final KeyStoreResource truststore = aKeyStoreResource()
            .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
            .build();

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
            metadataClientRule = new MetadataClientRule();
            vspClientRule = new VspClientRule();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        truststore.create();
    }

    @Rule
    public TranslatorAppRule translatorAppRule = new TranslatorAppRule(
            ConfigOverride.config("proxyNodeMetadataForConnectorNodeUrl", "http://proxy-node.uk"),
            ConfigOverride.config("connectorNodeIssuerId", "http://connector-node:8080/ConnectorMetadata"),
            ConfigOverride.config("vspConfiguration.url", vspClientRule.baseUri() + "/vsp"),
            ConfigOverride.config("connectorFacingSigningKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("connectorFacingSigningKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("connectorFacingSigningKeyPair.privateKey.key", TEST_PRIVATE_KEY),
            ConfigOverride.config("keyRetrieverServiceName", "config")
    );
}
