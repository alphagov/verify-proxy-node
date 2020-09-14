package uk.gov.ida.notification.translator.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import uk.gov.ida.notification.apprule.rules.AbstractSamlAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.AppRule;
import uk.gov.ida.notification.apprule.rules.TestMetatronResource;
import uk.gov.ida.notification.translator.TranslatorApplication;
import uk.gov.ida.notification.translator.apprule.rules.StubVspResource;
import uk.gov.ida.notification.translator.configuration.TranslatorConfiguration;

import static uk.gov.ida.notification.apprule.rules.TestMetadataResource.PROXY_NODE_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_EC_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_EC_CERT;

public class TranslatorAppRuleTestBase extends AbstractSamlAppRuleTestBase {

    @ClassRule
    public static final DropwizardClientRule vspClientRule = createInitialisedClientRule(new StubVspResource());

    @ClassRule
    public static final DropwizardClientRule metatronClientRule = createInitialisedClientRule(new TestMetatronResource());

    @ClassRule
    public static final AppRule<TranslatorConfiguration> translatorAppRule = new AppRule<>(
            TranslatorApplication.class,
            ConfigOverride.config("vspConfiguration.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("metatronUri", metatronClientRule.baseUri().toString()),
            ConfigOverride.config("proxyNodeEntityId", PROXY_NODE_ENTITY_ID),
            ConfigOverride.config("credentialConfiguration.type", "file"),
            ConfigOverride.config("credentialConfiguration.publicKey.type", "x509"),
            ConfigOverride.config("credentialConfiguration.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("credentialConfiguration.privateKey.key", TEST_PRIVATE_KEY)
    );

    @ClassRule
    public static final AppRule<TranslatorConfiguration> translatorAppRuleWithECSigning = new AppRule<>(
            TranslatorApplication.class,
            ConfigOverride.config("vspConfiguration.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("metatronUri", metatronClientRule.baseUri().toString()),
            ConfigOverride.config("proxyNodeEntityId", PROXY_NODE_ENTITY_ID),
            ConfigOverride.config("credentialConfiguration.type", "file"),
            ConfigOverride.config("credentialConfiguration.publicKey.type", "x509"),
            ConfigOverride.config("credentialConfiguration.publicKey.cert", TEST_PUBLIC_EC_CERT),
            ConfigOverride.config("credentialConfiguration.privateKey.key", TEST_PRIVATE_EC_KEY)
    );

}
