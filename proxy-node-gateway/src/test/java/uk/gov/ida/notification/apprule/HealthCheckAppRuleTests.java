package uk.gov.ida.notification.apprule;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderResource;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckAppRuleTests extends GatewayAppRuleTestBase {
    @ClassRule
    public static final DropwizardClientRule translatorClientRule = new DropwizardClientRule(new TestTranslatorResource());

    @ClassRule
    public static final DropwizardClientRule espClientRule = new DropwizardClientRule(new TestEidasSamlResource());

    @ClassRule
    public static final DropwizardClientRule vspClientRule = new DropwizardClientRule(new TestVerifyServiceProviderResource());

    @Rule
    public GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response")
    );

    @Test
    public void shouldExposeHealthCheck() throws Exception {
        Response response = proxyNodeAppRule.target("healthcheck", proxyNodeAppRule.getAdminPort())
            .request()
            .get();

        String healthcheck = response.readEntity(String.class);

        assertThat(healthcheck).contains("\"gateway\":{\"healthy\":true}");
    }
}
