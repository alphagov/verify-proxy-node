package uk.gov.ida.notification.apprule;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardClientExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.GatewayApplication;
import uk.gov.ida.notification.GatewayConfiguration;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlClientErrorResource;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlResource;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlServerErrorResource;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlServerValidationResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderServerErrorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderServerValidationResource;
import uk.gov.ida.notification.helpers.HtmlHelpers;
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

@ExtendWith(DropwizardExtensionsSupport.class)
public class EidasAuthnRequestAppRuleTests extends GatewayAppRuleTestBase {

    private static final String MOCKED_REDIS_URL = setupTestRedis();

    public static final DropwizardClientExtension translatorClientRule = createInitialisedClientExtension(new TestTranslatorResource());

    public static final DropwizardClientExtension espClientRule = createInitialisedClientExtension(new TestEidasSamlResource());

    public static final DropwizardClientExtension espClientServerErrorRule = createInitialisedClientExtension(new TestEidasSamlServerErrorResource());

    public static final DropwizardClientExtension espClientServerValidationRule = createInitialisedClientExtension(new TestEidasSamlServerValidationResource());

    public static final DropwizardClientExtension espClientClientErrorRule = createInitialisedClientExtension(new TestEidasSamlClientErrorResource());

    public static final DropwizardClientExtension vspClientRule = createInitialisedClientExtension(new TestVerifyServiceProviderResource());

    public static final DropwizardClientExtension vspClientServerErrorRule = createInitialisedClientExtension(new TestVerifyServiceProviderServerErrorResource());

    public static final DropwizardClientExtension vspClientServerValidationRule = createInitialisedClientExtension(new TestVerifyServiceProviderServerValidationResource());

    public static final DropwizardAppExtension<GatewayConfiguration> proxyNodeAppRule = new DropwizardAppExtension(
            GatewayApplication.class,
            ResourceHelpers.resourceFilePath("config.yml"),
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL),
            ConfigOverride.config("errorPageRedirectUrl", "https://www.integration.signin.service.gov.uk/proxy-node-error"),
            ConfigOverride.config("server.applicationConnectors[0].port", "7220"),
            ConfigOverride.config("server.adminConnectors[0].port", "7221")
    );

    public static final DropwizardAppExtension<GatewayConfiguration> proxyNodeEspServerErrorAppRule = new DropwizardAppExtension(
            GatewayApplication.class,
            ResourceHelpers.resourceFilePath("config.yml"),
            ConfigOverride.config("eidasSamlParserService.url", espClientServerErrorRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL),
            ConfigOverride.config("errorPageRedirectUrl", "https://www.integration.signin.service.gov.uk/proxy-node-error"),
            ConfigOverride.config("server.applicationConnectors[0].port", "7230"),
            ConfigOverride.config("server.adminConnectors[0].port", "7231")
    );

    public static final DropwizardAppExtension<GatewayConfiguration> proxyNodeEspServerValidationAppRule = new DropwizardAppExtension(
            GatewayApplication.class,
            ResourceHelpers.resourceFilePath("config.yml"),
            ConfigOverride.config("eidasSamlParserService.url", espClientServerValidationRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL),
            ConfigOverride.config("errorPageRedirectUrl", "https://www.integration.signin.service.gov.uk/proxy-node-error"),
            ConfigOverride.config("server.applicationConnectors[0].port", "7240"),
            ConfigOverride.config("server.adminConnectors[0].port", "7240"),
            ConfigOverride.config("server.adminConnectors[0].port", "7241")
    );

    public static final DropwizardAppExtension<GatewayConfiguration> proxyNodeEspClientErrorAppRule = new DropwizardAppExtension(
            GatewayApplication.class,
            ResourceHelpers.resourceFilePath("config.yml"),
            ConfigOverride.config("eidasSamlParserService.url", espClientClientErrorRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL),
            ConfigOverride.config("errorPageRedirectUrl", "https://www.integration.signin.service.gov.uk/proxy-node-error"),
            ConfigOverride.config("server.applicationConnectors[0].port", "7250"),
            ConfigOverride.config("server.adminConnectors[0].port", "7251")
    );

    public static final DropwizardAppExtension<GatewayConfiguration> proxyNodeVspServerErrorAppRule = new DropwizardAppExtension(
            GatewayApplication.class,
            ResourceHelpers.resourceFilePath("config.yml"),
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientServerErrorRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL),
            ConfigOverride.config("errorPageRedirectUrl", "https://www.integration.signin.service.gov.uk/proxy-node-error"),
            ConfigOverride.config("server.applicationConnectors[0].port", "7260"),
            ConfigOverride.config("server.adminConnectors[0].port", "7261")
    );

    public static final DropwizardAppExtension<GatewayConfiguration> proxyNodeVspServerValidationAppRule = new DropwizardAppExtension(
            GatewayApplication.class,
            ResourceHelpers.resourceFilePath("config.yml"),
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientServerValidationRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.url", MOCKED_REDIS_URL),
            ConfigOverride.config("errorPageRedirectUrl", "https://www.integration.signin.service.gov.uk/proxy-node-error"),
            ConfigOverride.config("server.applicationConnectors[0].port", "7270"),
            ConfigOverride.config("server.adminConnectors[0].port", "7271")
    );

    @Mock
    private static Appender<ILoggingEvent> appender;

    @Captor
    private static ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor;

    @AfterAll
    public static void tearDown() {
        killTestRedis();
    }

    @Test
    public void bindingsReturnHubAuthnRequestForm() throws Throwable {
       // Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);
        // logger.addAppender(appender);

        assertGoodRequest(buildAuthnRequest());
    }

    @Test
    public void accessingWrongPathRedirectsToErrorPage() throws URISyntaxException {
        final Response response = proxyNodeAppRule.client().target("/invalid-path").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void accessingProxyNodeDirectlyRedirectsToErrorPage() throws URISyntaxException {
        final Response response = proxyNodeAppRule.client().target("/SAML2/SSO/POST").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void serverErrorResponseFromEspRedirectsToErrorPage() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeEspServerErrorAppRule.client(), proxyNodeEspServerErrorAppRule.getLocalPort());

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void server400ResponseFromEspRedirectsToErrorPage() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeEspServerValidationAppRule.client(), proxyNodeEspServerValidationAppRule.getLocalPort());

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void clientErrorResponseFromEspRedirectsToErrorPage() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeEspClientErrorAppRule.client(), proxyNodeEspServerValidationAppRule.getLocalPort());

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void serverErrorResponseFromVspRedirectsToErrorPage() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeVspServerErrorAppRule.client(), proxyNodeEspServerValidationAppRule.getLocalPort());

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void server400ResponseFromVspRedirectsToErrorPage() throws Exception {
        Response response = postEidasAuthnRequest(buildAuthnRequest(), proxyNodeVspServerValidationAppRule.client(), proxyNodeEspServerValidationAppRule.getLocalPort());

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void requestWithNoIdReirectsToErrorPage() throws Exception {
        AuthnRequest requestWithoutId = buildAuthnRequestWithoutId();
        Response response = postEidasAuthnRequest(requestWithoutId, proxyNodeVspServerErrorAppRule.client(), proxyNodeEspServerValidationAppRule.getLocalPort());

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    @Test
    public void badlyFormattedRequestRedirectsToErrorPage() throws Exception {
        Response response = postInvalidEidasAuthnRequest(buildAuthnRequest(), proxyNodeVspServerErrorAppRule.client());

        assertThat(response.getStatus()).isEqualTo(Response.Status.SEE_OTHER.getStatusCode());
        assertThat(response.getHeaderString("Location")).isEqualTo(ERROR_PAGE_REDIRECT_URL);
    }

    private void assertGoodRequest(AuthnRequest request) throws Throwable {
        assertGoodSamlSuccessResponse(postEidasAuthnRequest(request, proxyNodeAppRule.client(), proxyNodeEspServerValidationAppRule.getLocalPort()));
        assertGoodSamlSuccessResponse(redirectEidasAuthnRequest(request, proxyNodeAppRule.client()));
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
