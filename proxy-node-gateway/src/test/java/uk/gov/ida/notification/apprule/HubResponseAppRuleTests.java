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
import org.slf4j.LoggerFactory;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.apprule.rules.RedisTestRule;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorClientErrorResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorServerErrorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderResource;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.helpers.ValidationTestDataUtils;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.shared.Urls;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.notification.apprule.rules.TestTranslatorClientErrorResource.SAML_ERROR_BLOB;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_DESTINATION_URL;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_ENTITY_ID;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.MESSAGE_EGRESS;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.MESSAGE_INGRESS;

@RunWith(MockitoJUnitRunner.class)
public class HubResponseAppRuleTests extends GatewayAppRuleTestBase {

    private static final int EMBEDDED_REDIS_PORT = 6380;
    private static final String ERROR_PAGE_REDIRECT_URL = "https://proxy-node-error-page";
    private static final TestTranslatorResource TEST_TRANSLATOR_RESOURCE = new TestTranslatorResource();
    private static final String REDIS_MOCK_URI = setupTestRedis();
    private static final Form POST_FORM = new Form()
            .param(SamlFormMessageType.SAML_RESPONSE, ValidationTestDataUtils.SAMLPLE_HUB_SAML_RESPONSE)
            .param("RelayState", "relay-state");

    @ClassRule
    public static final DropwizardClientRule translatorClientRule = createInitialisedClientRule(TEST_TRANSLATOR_RESOURCE);

    @ClassRule
    public static final DropwizardClientRule translatorClientServerErrorRule = createInitialisedClientRule(new TestTranslatorServerErrorResource());

    @ClassRule
    public static final DropwizardClientRule translatorClientClientErrorRule = createInitialisedClientRule(new TestTranslatorClientErrorResource());

    @ClassRule
    public static final DropwizardClientRule espClientRule = createInitialisedClientRule(new TestEidasSamlResource());

    @ClassRule
    public static final DropwizardClientRule vspClientRule = createInitialisedClientRule(new TestVerifyServiceProviderResource());

    @ClassRule
    public static final RedisTestRule embeddedRedis = new RedisTestRule(EMBEDDED_REDIS_PORT);

    @ClassRule
    public static final GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri().toString()),
            ConfigOverride.config("redisService.url", REDIS_MOCK_URI),
            ConfigOverride.config("errorPageRedirectUrl", ERROR_PAGE_REDIRECT_URL)
    );

    @ClassRule
    public static final GatewayAppRule proxyNodeAppRuleNoErrorPageUrl = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri().toString()),
            ConfigOverride.config("redisService.url", REDIS_MOCK_URI)
    );

    @ClassRule
    public static final GatewayAppRule proxyNodeAppRuleEmbeddedRedis = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri().toString()),
            ConfigOverride.config("redisService.url", "redis://localhost:" + EMBEDDED_REDIS_PORT)
    );

    @ClassRule
    public static final GatewayAppRule proxyNodeServerErrorAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientServerErrorRule.baseUri().toString()),
            ConfigOverride.config("redisService.url", REDIS_MOCK_URI)
    );

    @ClassRule
    public static final GatewayAppRule proxyNodeClientErrorAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientClientErrorRule.baseUri().toString()),
            ConfigOverride.config("redisService.url", REDIS_MOCK_URI)
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
    public void hubResponseReturnsHtmlFormWithSamlBlob() throws Exception {
        NewCookie sessionCookie = postSAMLRequest(proxyNodeAppRule);
        Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);
        logger.addAppender(appender);

        Response response = proxyNodeAppRule
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
                .request()
                .cookie(sessionCookie)
                .post(Entity.form(POST_FORM));
        assertLogsIngressEgress();
        assertSuccessfulResponse(response);
    }

    @Test
    public void redisCanStoreIssuerEntityIdInSession() throws Throwable {
        Response response = proxyNodeAppRuleEmbeddedRedis
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
                .request()
                .cookie(postSAMLRequest(proxyNodeAppRuleEmbeddedRedis))
                .post(Entity.form(POST_FORM));

        assertThat(response.getStatus()).isEqualTo(200);

        final List<HubResponseTranslatorRequest> translatorArgs = TEST_TRANSLATOR_RESOURCE.getTranslatorArgs();
        assertThat(translatorArgs.get(0).getConnectorEntityId()).isEqualTo(URI.create(SAMPLE_ENTITY_ID));
        assertThat(translatorArgs.size()).isOne();
    }

    @Test
    public void redirectsToErrorPageIfSessionMissingException() throws URISyntaxException {
        Response response = proxyNodeAppRule
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE, false)
                .request()
                .post(Entity.form(POST_FORM));

        assertShowProxyNodeErrorPage(response);
    }

    @Test
    public void serverErrorResponseFromTranslatorReturns200SamlErrorResponse() throws Exception {
        Response response = proxyNodeServerErrorAppRule
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
                .request()
                .cookie(postSAMLRequest(proxyNodeServerErrorAppRule))
                .post(Entity.form(POST_FORM));

        assertGoodSamlErrorResponse(response);
    }

    @Test
    public void clientErrorResponseFromTranslatorReturns200SamlErrorResponse() throws Exception {
        Response response = proxyNodeClientErrorAppRule
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
                .request()
                .cookie(postSAMLRequest(proxyNodeClientErrorAppRule))
                .post(Entity.form(POST_FORM));

        assertGoodSamlErrorResponse(response);
    }

    @Test
    public void testThatASuccessfulJourneyClearsSession() throws Exception {
        NewCookie samlRequestWithCookie = postSAMLRequest(proxyNodeAppRule);
        Response response = proxyNodeAppRule
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
                .request()
                .cookie(samlRequestWithCookie)
                .post(Entity.form(POST_FORM));
        assertSuccessfulResponse(response);
        Response samlResponseAgain = proxyNodeAppRule
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
                .request()
                .cookie(samlRequestWithCookie)
                .post(Entity.form(POST_FORM));
        assertThatASAMLResponseCannotBeReplayed(samlRequestWithCookie, samlResponseAgain);
    }

    @Test
    public void testThatABadResponseFromTranslatorProducingSAMLErrorResponseClearsSession() throws Exception {
        NewCookie samlRequestWithCookie = postSAMLRequest(proxyNodeServerErrorAppRule);
        Response response = proxyNodeServerErrorAppRule
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
                .request()
                .cookie(samlRequestWithCookie)
                .post(Entity.form(POST_FORM));
        assertGoodSamlErrorResponse(response);
        Response samlResponseAgain = proxyNodeAppRule
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
                .request()
                .cookie(samlRequestWithCookie)
                .post(Entity.form(POST_FORM));
        assertThatASAMLResponseCannotBeReplayed(samlRequestWithCookie, samlResponseAgain);
    }

    @Test
    public void testThatABadResponseResultingInProxyNodeErrorPageClearsSession() throws Exception {
        NewCookie samlRequestWithCookie = postSAMLRequest(proxyNodeServerErrorAppRule);
        Response response = proxyNodeClientErrorAppRule
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
                .request()
                .cookie(samlRequestWithCookie)
                .post(Entity.form(POST_FORM));
        assertShowProxyNodeErrorPage(response);
        Response samlResponseAgain = proxyNodeAppRule
                .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
                .request()
                .cookie(samlRequestWithCookie)
                .post(Entity.form(POST_FORM));
        assertThatASAMLResponseCannotBeReplayed(samlRequestWithCookie, samlResponseAgain);
    }

    private void assertThatASAMLResponseCannotBeReplayed(NewCookie requestCookie, Response response) {
        assertThat(requestCookie.getValue()).isNotNull();
        assertShowProxyNodeErrorPage(response);
        NewCookie responseCookie = response.getCookies().get("gateway-session");
        assertThat(responseCookie.getValue()).isNotNull();
        assertThat(responseCookie.getValue()).isNotEqualTo(requestCookie.getValue());
    }

    private void assertLogsIngressEgress() {
        verify(appender, atLeastOnce()).doAppend(loggingEventArgumentCaptor.capture());
        final List<ILoggingEvent> logEvents = loggingEventArgumentCaptor.getAllValues();

        assertThat(logEvents).filteredOn(e -> e.getMessage().equals(MESSAGE_INGRESS)).hasSizeGreaterThanOrEqualTo(1);
        assertThat(logEvents).filteredOn(e -> e.getMessage().equals(MESSAGE_EGRESS)).hasSizeGreaterThanOrEqualTo(1);
    }

    private NewCookie postSAMLRequest(GatewayAppRule appRule) throws Exception {
        return postEidasAuthnRequest(buildAuthnRequest(), appRule).getCookies().get("gateway-session");
    }

    private void assertGoodSamlErrorResponse(Response response) throws XPathExpressionException, ParserConfigurationException {
        final String htmlString = response.readEntity(String.class);

        assertThat(response.getStatus()).isEqualTo(200);

        HtmlHelpers.assertXPath(
                htmlString,
                String.format("//form[@action='%s']/input[@name='SAMLResponse'][@value='%s']", SAMPLE_DESTINATION_URL, SAML_ERROR_BLOB)
        );

        HtmlHelpers.assertXPath(
                htmlString,
                String.format("//form[@action='%s']/input[@name='RelayState'][@value='relay-state']", SAMPLE_DESTINATION_URL)
        );
    }

    private void assertShowProxyNodeErrorPage(Response response) {
        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    private void assertSuccessfulResponse(Response response) throws XPathExpressionException, ParserConfigurationException {
        assertThat(response.getStatus()).isEqualTo(200);
        final String htmlString = response.readEntity(String.class);
        HtmlHelpers.assertXPath(
                htmlString,
                String.format(
                        "//form[@action='%s']/input[@name='SAMLResponse'][@value='%s']",
                        SAMPLE_DESTINATION_URL,
                        TestTranslatorResource.SAML_SUCCESS_BLOB));

        HtmlHelpers.assertXPath(
                htmlString,
                String.format(
                        "//form[@action='%s']/input[@name='RelayState'][@value='relay-state']",
                        SAMPLE_DESTINATION_URL));
    }
}
