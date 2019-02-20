package uk.gov.ida.notification.apprule;

import io.dropwizard.testing.ConfigOverride;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.EidasSamlParserClientRule;
import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderResource;
import uk.gov.ida.notification.apprule.rules.TranslatorClientRule;
import uk.gov.ida.notification.apprule.rules.VerifyServiceProviderClientRule;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.shared.Urls;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class HubResponseAppRuleTests extends GatewayAppRuleTestBase {

    @ClassRule
    public static final TranslatorClientRule translatorClientRule = new TranslatorClientRule();

    @ClassRule
    public static final EidasSamlParserClientRule<TestEidasSamlResource> espClientRule = new EidasSamlParserClientRule<>(new TestEidasSamlResource());

    @ClassRule
    public static final VerifyServiceProviderClientRule<TestVerifyServiceProviderResource> vspClientRule = new VerifyServiceProviderClientRule<>(new TestVerifyServiceProviderResource());

    @Rule
    public GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri().toString())
    );

    @Test
    public void hubResponseReturnsHtmlFormWithSamlBlob() throws Exception{
        Form postForm = new Form()
            .param(SamlFormMessageType.SAML_RESPONSE, Base64.encodeAsString("I'm going to be a SAML blob"))
            .param("RelayState", "relay-state");

        Response response = proxyNodeAppRule
            .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
            .request()
            .cookie(getSessionCookie())
            .post(Entity.form(postForm));

        assertEquals(200, response.getStatus());

        String htmlString = response.readEntity(String.class);
        HtmlHelpers.assertXPath(
            htmlString,
            String.format(
                "//form[@action='http://connector-node.com']/input[@name='SAMLResponse'][@value='%s']",
                TestTranslatorResource.SAML_BLOB
            )
        );
        HtmlHelpers.assertXPath(
            htmlString,
            "//form[@action='http://connector-node.com']/input[@name='RelayState'][@value='relay-state']"
        );
    }

    private NewCookie getSessionCookie() throws Exception {
        return postEidasAuthnRequest(buildAuthnRequest(), proxyNodeAppRule).getCookies().get("gateway-session");
    }

}