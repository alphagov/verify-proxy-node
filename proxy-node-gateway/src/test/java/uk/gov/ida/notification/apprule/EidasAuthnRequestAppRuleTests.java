package uk.gov.ida.notification.apprule;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.LoggerFactory;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlClientErrorResource;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlResource;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlServerErrorResource;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlServerValidationResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderServerErrorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderServerValidationResource;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.notification.apprule.rules.GatewayAppRule.ERROR_PAGE_REDIRECT_URL;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.MESSAGE_EGRESS;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.MESSAGE_INGRESS;

@RunWith(MockitoJUnitRunner.class)
public class EidasAuthnRequestAppRuleTests extends GatewayAppRuleTestBase {

    private static final String MOCKED_REDIS_URL = setupTestRedis();

    @ClassRule
    public static final DropwizardClientRule translatorClientRule = createInitialisedClientRule(new TestTranslatorResource());

    @ClassRule
    public static final DropwizardClientRule espClientRule = createInitialisedClientRule(new TestEidasSamlResource());

    @ClassRule
    public static final DropwizardClientRule espClientServerErrorRule = createInitialisedClientRule(new TestEidasSamlServerErrorResource());

    @ClassRule
    public static final DropwizardClientRule espClientServerValidationRule = createInitialisedClientRule(new TestEidasSamlServerValidationResource());

    @ClassRule
    public static final DropwizardClientRule espClientClientErrorRule = createInitialisedClientRule(new TestEidasSamlClientErrorResource());

    @ClassRule
    public static final DropwizardClientRule vspClientRule = createInitialisedClientRule(new TestVerifyServiceProviderResource());

    @ClassRule
    public static final DropwizardClientRule vspClientServerErrorRule = createInitialisedClientRule(new TestVerifyServiceProviderServerErrorResource());

    @ClassRule
    public static final DropwizardClientRule vspClientServerValidationRule = createInitialisedClientRule(new TestVerifyServiceProviderServerValidationResource());

    @ClassRule
    public static final GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL)
    );

    @ClassRule
    public static final GatewayAppRule proxyNodeEspServerErrorAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientServerErrorRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL)
    );

    @ClassRule
    public static final GatewayAppRule proxyNodeEspServerValidationAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientServerValidationRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL)
    );

    @ClassRule
    public static final GatewayAppRule proxyNodeEspClientErrorAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientClientErrorRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL)
    );

    @ClassRule
    public static final GatewayAppRule proxyNodeVspServerErrorAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientServerErrorRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL)
    );

    @ClassRule
    public static final GatewayAppRule proxyNodeVspServerValidationAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientServerValidationRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL)
    );

    @Mock
    private static Appender<ILoggingEvent> appender;

    @Captor
    private static ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor;

    @AfterClass
    public static void tearDown() {
        killTestRedis();
    }

    @Test
    public void bindingsReturnHubAuthnRequestForm() throws Throwable {
        Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);
        logger.addAppender(appender);

        assertGoodRequest(buildAuthnRequest());
    }

    @Test
    public void accessingWrongPathGenerates404() throws URISyntaxException {
        final Response response = proxyNodeAppRule.target("/invalid-path", false).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void accessingRootRedirectsToErrorPage() throws URISyntaxException {
        final Response response = proxyNodeAppRule.target("/", false).request().get();

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
    public void server400ResponseFromEspRedirectsToErrorPage() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeEspServerValidationAppRule, false);

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

    @Test
    public void server400ResponseFromVspRedirectsToErrorPage() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeVspServerValidationAppRule, false);

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void requestWithNoIdReirectsToErrorPage() throws Exception {
        AuthnRequest requestWithoutId = buildAuthnRequestWithoutId();
        Response response = postEidasAuthnRequest(requestWithoutId, proxyNodeVspServerErrorAppRule, false);

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void badlyFormattedRequestRedirectsToErrorPage() throws Exception {
        Response response = postInvalidEidasAuthnRequest(buildAuthnRequest(), proxyNodeVspServerErrorAppRule, false);

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    private void assertGoodRequest(AuthnRequest request) throws Throwable {
        assertGoodSamlSuccessResponse(postEidasAuthnRequest(request, proxyNodeAppRule));
        assertGoodSamlSuccessResponse(redirectEidasAuthnRequest(request, proxyNodeAppRule));
        assertLogsIngressEgress();
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

    private void assertLogsIngressEgress() {
        verify(appender, atLeastOnce()).doAppend(loggingEventArgumentCaptor.capture());
        final List<ILoggingEvent> logEvents = loggingEventArgumentCaptor.getAllValues();

        assertThat(logEvents).filteredOn(e -> e.getMessage().equals(MESSAGE_INGRESS)).hasSize(2);
        assertThat(logEvents).filteredOn(e -> e.getMessage().equals(MESSAGE_EGRESS)).hasSize(2);
    }

    private String getHtmlStringFromResponse(Response response) {
        return response.readEntity(String.class);
    }
}
