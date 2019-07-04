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
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.notification.apprule.rules.GatewayAppRule.ERROR_PAGE_REDIRECT_URL;

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
            ConfigOverride.config("redisService.url", mockedRedisUrl),
            ConfigOverride.config("metadataPublishingConfiguration.metadataResourceFilePath", ""),
            ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", "")
    );

    @Rule
    public GatewayAppRule proxyNodeEspServerErrorAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientServerErrorRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", mockedRedisUrl),
            ConfigOverride.config("metadataPublishingConfiguration.metadataResourceFilePath", ""),
            ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", "")
    );

    @Rule
    public GatewayAppRule proxyNodeEspClientErrorAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientClientErrorRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", mockedRedisUrl),
            ConfigOverride.config("metadataPublishingConfiguration.metadataResourceFilePath", ""),
            ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", "")
    );

    @Rule
    public GatewayAppRule proxyNodeVspServerErrorAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientServerErrorRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", mockedRedisUrl),
            ConfigOverride.config("metadataPublishingConfiguration.metadataResourceFilePath", ""),
            ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", "")
    );

    @Test
    public void bindingsReturnHubAuthnRequestForm() throws Throwable {
        assertGoodRequest(buildAuthnRequest());
    }

    @Test
    public void accessingWrongPathRedirectsToErrorPage() throws URISyntaxException {
        final Response response = proxyNodeAppRule.target("/invalid-path", false).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void accessingProxyNodeDirectlyRedirectsToErrorPage() throws URISyntaxException {
        final Response response = proxyNodeAppRule.target("/SAML2/SSO/POST", false).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void serverErrorResponseFromEspRedirectsToErrorPage() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeEspServerErrorAppRule, false);

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void clientErrorResponseFromEspRedirectsToErrorPage() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeEspClientErrorAppRule, false);

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void serverErrorResponseFromVspRedirectsToErrorPage() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeVspServerErrorAppRule, false);

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    private void assertGoodRequest(AuthnRequest request) throws Throwable {
        assertGoodSamlSuccessResponse(postEidasAuthnRequest(request, proxyNodeAppRule));
        assertGoodSamlSuccessResponse(redirectEidasAuthnRequest(request, proxyNodeAppRule));
    }

    private void assertGoodSamlSuccessResponse(Response response) throws XPathExpressionException, ParserConfigurationException {
        final String htmlString = getHtmlStringFromResponse(response);

        HtmlHelpers.assertXPath(
                htmlString,
                String.format("//form[@action='http://www.hub.com']/input[@name='SAMLRequest'][@value='%s']", TestVerifyServiceProviderResource.ENCODED_SAML_BLOB)
        );
        HtmlHelpers.assertXPath(
                htmlString,
                String.format("//form[@action='http://www.hub.com']/input[@name='RelayState'][@value='%s']",
                              response.getHeaders().getFirst(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name())
                              )
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
