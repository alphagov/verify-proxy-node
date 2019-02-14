package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;

import org.junit.ClassRule;
import org.junit.Rule;

import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.apprule.rules.TranslatorClientRule;

public class GatewayAppRuleTestBase {

    @ClassRule
    public static final TranslatorClientRule translatorClientRule = new TranslatorClientRule();

    @Rule
    public GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", "http://eidas-saml-parser/eidasAuthnRequest"),
            ConfigOverride.config("verifyServiceProviderService.url", "http://verify-service-provider/"),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response")
    );
}
