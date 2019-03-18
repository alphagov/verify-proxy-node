package uk.gov.ida.notification.apprule;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlClientErrorResource;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlResource;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlServerErrorResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderServerErrorResource;
import uk.gov.ida.notification.helpers.HtmlHelpers;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EidasAuthnRequestAppRuleTests extends GatewayAppRuleTestBase {

    @ClassRule
    public static final DropwizardClientRule translatorClientRule = new DropwizardClientRule(new TestTranslatorResource());

    @ClassRule
    public static final DropwizardClientRule espClientRule = new DropwizardClientRule(new TestEidasSamlResource());

    @ClassRule
    public static final DropwizardClientRule espClientServerErrorRule = new DropwizardClientRule(new TestEidasSamlServerErrorResource());

    @ClassRule
    public static final DropwizardClientRule espClientClientErrorRule = new DropwizardClientRule(new TestEidasSamlClientErrorResource());

    @ClassRule
    public static final DropwizardClientRule vspClientRule = new DropwizardClientRule(new TestVerifyServiceProviderResource());

    @ClassRule
    public static final DropwizardClientRule vspClientServerErrorRule = new DropwizardClientRule(new TestVerifyServiceProviderServerErrorResource());

    private String mockedRedisUrl = this.setupTestRedis();

    @Rule
    public GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
        ConfigOverride.config("redisService.url", mockedRedisUrl)
    );

    @Rule
    public GatewayAppRule proxyNodeEspServerErrorAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientServerErrorRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
        ConfigOverride.config("redisService.url", mockedRedisUrl)
    );

    @Rule
    public GatewayAppRule proxyNodeEspClientErrorAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientClientErrorRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
        ConfigOverride.config("redisService.url", mockedRedisUrl)
    );

    @Rule
    public GatewayAppRule proxyNodeVspServerErrorAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientServerErrorRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
        ConfigOverride.config("redisService.url", mockedRedisUrl)
    );

    @Test
    public void bindingsReturnHubAuthnRequestForm() throws Throwable {
        assertGoodRequest(buildAuthnRequest());
    }

    @Test
    public void serverErrorResponseFromEspReturns500() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeEspServerErrorAppRule);

        assertThat(response.getStatus()).isEqualTo(500);
        HtmlHelpers.assertXPath(
            getHtmlStringFromResponse(response),
            "//div[@class='issues'][text()='Something went wrong when contacting the ESP']"
        );
    }

    @Test
    public void clientErrorResponseFromEspReturns400() throws Exception {
        AuthnRequest request = buildAuthnRequest();
        Response response = postEidasAuthnRequest(request, proxyNodeEspClientErrorAppRule);

        assertThat(response.getStatus()).isEqualTo(400);
        HtmlHelpers.assertXPath(
            getHtmlStringFromResponse(response),
            "//div[@class='issues'][text()='Something went wrong when contacting the ESP']"
        );
    }

    @Test
    public void serverErrorResponseFromVspLogsAndReturns500() throws Exception {
        Response response = postEidasAuthnRequest(
            buildAuthnRequest(),
            proxyNodeVspServerErrorAppRule
        );

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        HtmlHelpers.assertXPath(
            getHtmlStringFromResponse(response),
            "//div[@class='issues'][text()='Something went wrong when contacting the VSP']"
        );
    }

    private void assertGoodRequest(AuthnRequest request) throws Throwable {
        assertGoodResponse(postEidasAuthnRequest(request, proxyNodeAppRule));
        assertGoodResponse(redirectEidasAuthnRequest(request, proxyNodeAppRule));
    }

    private void assertGoodResponse(Response response) throws IOException, XPathExpressionException, ParserConfigurationException {
        String htmlString = getHtmlStringFromResponse(response);

        HtmlHelpers.assertXPath(
            htmlString,
            String.format("//form[@action='http://www.hub.com']/input[@name='SAMLRequest'][@value='%s']", TestVerifyServiceProviderResource.ENCODED_SAML_BLOB)
        );
        HtmlHelpers.assertXPath(
            htmlString,
            "//form[@action='http://www.hub.com']/input[@name='RelayState'][@value='relay-state']"
        );
    }

    private String getHtmlStringFromResponse(Response response) {
        return response.readEntity(String.class);
    }

    @AfterAll
    public void tearDown() {
        this.killTestRedis();
    }
}
