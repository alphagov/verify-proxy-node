package uk.gov.ida.notification.translator.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.translator.apprule.rules.TranslatorAppRule;
import uk.gov.ida.notification.translator.apprule.rules.VspClientRule;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;

public class TranslatorAppRuleTestBase {

    @ClassRule
    public static final VspClientRule vspClientRule;

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
            vspClientRule = new VspClientRule();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
