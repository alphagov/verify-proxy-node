package uk.gov.ida.notification.translator.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.translator.apprule.rules.StubVspResource;
import uk.gov.ida.notification.translator.apprule.rules.TranslatorAppRule;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;

public class TranslatorAppRuleTestBase {

    @ClassRule
    public static final DropwizardClientRule vspClientRule;

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
            vspClientRule = new DropwizardClientRule(new StubVspResource());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public TranslatorAppRule translatorAppRule = new TranslatorAppRule(
            ConfigOverride.config("proxyNodeMetadataForConnectorNodeUrl", "http://proxy-node.uk"),
            ConfigOverride.config("connectorNodeIssuerId", "http://connector-node:8080/ConnectorMetadata"),
            ConfigOverride.config("vspConfiguration.url", vspClientRule.baseUri() + "/vsp"),
            ConfigOverride.config("connectorNodeNationalityCode", "NATIONALITY_CODE"),
            ConfigOverride.config("credentialConfiguration.type", "file"),
            ConfigOverride.config("credentialConfiguration.publicKey.type", "x509"),
            ConfigOverride.config("credentialConfiguration.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("credentialConfiguration.privateKey.key", TEST_PRIVATE_KEY)
    );
}
