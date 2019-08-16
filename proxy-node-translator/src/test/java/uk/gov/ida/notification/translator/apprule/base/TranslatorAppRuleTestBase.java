package uk.gov.ida.notification.translator.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import uk.gov.ida.notification.apprule.rules.AbstractSamlAppRuleTestBase;
import uk.gov.ida.notification.translator.apprule.rules.StubVspResource;
import uk.gov.ida.notification.translator.apprule.rules.TranslatorAppRule;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;

public class TranslatorAppRuleTestBase extends AbstractSamlAppRuleTestBase {

    @ClassRule
    public static final DropwizardClientRule vspClientRule = createInitialisedClientRule(new StubVspResource());

    @ClassRule
    public static final TranslatorAppRule translatorAppRule = new TranslatorAppRule(
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
