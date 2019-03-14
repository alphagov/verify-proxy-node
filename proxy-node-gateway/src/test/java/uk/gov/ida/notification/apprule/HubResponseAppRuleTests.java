package uk.gov.ida.notification.apprule;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.mockito.ArgumentCaptor;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.apprule.rules.RedisTestRule;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorClientErrorResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorServerErrorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderResource;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.exceptions.mappers.SessionMissingExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.TranslatorResponseExceptionMapper;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.shared.Urls;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;

public class HubResponseAppRuleTests extends GatewayAppRuleTestBase {

    private static final int EMBEDDED_REDIS_PORT = 6380;

    private static List<HubResponseTranslatorRequest> translatorArgList = new ArrayList<>();

    private String redisMockURI = this.setupTestRedis();

    @ClassRule
    public static final DropwizardClientRule translatorClientRule = new DropwizardClientRule(new TestTranslatorResource(translatorArgList));

    @ClassRule
    public static final DropwizardClientRule translatorClientServerErrorRule = new DropwizardClientRule(new TestTranslatorServerErrorResource());

    @ClassRule
    public static final DropwizardClientRule translatorClientClientErrorRule = new DropwizardClientRule(new TestTranslatorClientErrorResource());

    @ClassRule
    public static final DropwizardClientRule espClientRule = new DropwizardClientRule(new TestEidasSamlResource());

    @ClassRule
    public static final DropwizardClientRule vspClientRule = new DropwizardClientRule(new TestVerifyServiceProviderResource());

    @ClassRule
    public static final RedisTestRule embeddedRedis = new RedisTestRule(EMBEDDED_REDIS_PORT);

    @Rule
    public GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri().toString()),
        ConfigOverride.config("redisService.url", redisMockURI)
    );

    @Rule
    public GatewayAppRule proxyNodeAppRuleEmbeddedRedis = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri().toString()),
        ConfigOverride.config("redisService.url", "redis://localhost:" + EMBEDDED_REDIS_PORT)
    );

    @Rule
    public GatewayAppRule proxyNodeServerErrorAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientServerErrorRule.baseUri().toString()),
        ConfigOverride.config("redisService.url", redisMockURI)
    );

    @Rule
    public GatewayAppRule proxyNodeClientErrorAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientClientErrorRule.baseUri().toString()),
        ConfigOverride.config("redisService.url", redisMockURI)
    );

    private final Form postForm = new Form()
        .param(SamlFormMessageType.SAML_RESPONSE, Base64.encodeAsString("I'm going to be a SAML blob"))
        .param("RelayState", "relay-state");

    private Handler logHandler;
    private ArgumentCaptor<LogRecord> captorLoggingEvent;

    @Before
    public void setup() {
        logHandler = mock(Handler.class);
        captorLoggingEvent = ArgumentCaptor.forClass(LogRecord.class);
    }

    @Test
    public void hubResponseReturnsHtmlFormWithSamlBlob() throws Exception {
        Response response = proxyNodeAppRule
            .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
            .request()
            .cookie(getSessionCookie(proxyNodeAppRule))
            .post(Entity.form(postForm));

        assertThat(200).isEqualTo(response.getStatus());

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

    @Test
    public void redisCanStoreCertificateInSession() throws Exception, Throwable {
        Response response = proxyNodeAppRuleEmbeddedRedis
            .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
            .request()
            .cookie(getSessionCookie(proxyNodeAppRuleEmbeddedRedis))
            .post(Entity.form(postForm));

        assertThat(200).isEqualTo(response.getStatus());

        assertThat(translatorArgList.get(0).getConnectorEncryptionCertificate()).isEqualTo(STUB_COUNTRY_PUBLIC_PRIMARY_CERT);
        assertThat(translatorArgList.size()).isOne();
    }

    @Test
    public void returnsErrorPageAndLogsIfSessionMissingException() throws Exception {
        Logger logger = Logger.getLogger(SessionMissingExceptionMapper.class.getName());
        logger.addHandler(logHandler);

        Response response = proxyNodeAppRule
            .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
            .request()
            .post(Entity.form(postForm));

        assertThat(400).isEqualTo(response.getStatus());

        String htmlString = response.readEntity(String.class);
        HtmlHelpers.assertXPath(
            htmlString,
            "//div[@class='issues'][text()='Something went wrong session should exist']"
        );

        verify(logHandler).publish(captorLoggingEvent.capture());
        assertThat(captorLoggingEvent.getValue().getLevel()).isEqualTo(WARNING);
        assertThat(captorLoggingEvent.getValue().getMessage())
            .matches("Session should exist for session_id: .+");
    }

    @Test
    public void serverErrorResponseFromTranslatorLogsAndReturns500() throws Exception {
        Logger logger = Logger.getLogger(TranslatorResponseExceptionMapper.class.getName());
        logger.addHandler(logHandler);

        Response response = proxyNodeServerErrorAppRule
            .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
            .request()
            .cookie(getSessionCookie(proxyNodeServerErrorAppRule))
            .post(Entity.form(postForm));

        assertThat(500).isEqualTo(response.getStatus());

        String htmlString = response.readEntity(String.class);
        HtmlHelpers.assertXPath(
            htmlString,
            "//div[@class='issues'][text()='Something went wrong with the Translator']"
        );

        verify(logHandler).publish(captorLoggingEvent.capture());
        assertThat(captorLoggingEvent.getValue().getLevel()).isEqualTo(WARNING);
        assertThat(captorLoggingEvent.getValue().getMessage())
            .matches("Exception calling translator for session '.*': Exception of type \\[REMOTE_SERVER_ERROR\\] whilst contacting uri:.*\n.*");
    }

    @Test
    public void clientErrorResponseFromTranslatorLogsAndReturns400() throws Exception {
        Logger logger = Logger.getLogger(TranslatorResponseExceptionMapper.class.getName());
        logger.addHandler(logHandler);

        Response response = proxyNodeClientErrorAppRule
            .target(Urls.GatewayUrls.GATEWAY_HUB_RESPONSE_RESOURCE)
            .request()
            .cookie(getSessionCookie(proxyNodeClientErrorAppRule))
            .post(Entity.form(postForm));

        assertThat(400).isEqualTo(response.getStatus());

        String htmlString = response.readEntity(String.class);
        HtmlHelpers.assertXPath(
            htmlString,
            "//div[@class='issues'][text()='Something went wrong with the Translator']"
        );

        verify(logHandler).publish(captorLoggingEvent.capture());
        assertThat(captorLoggingEvent.getValue().getLevel()).isEqualTo(WARNING);
        assertThat(captorLoggingEvent.getValue().getMessage())
            .matches("Exception calling translator for session '.*': Exception of type \\[CLIENT_ERROR\\] whilst contacting uri:.*\n.*");
    }

    private NewCookie getSessionCookie(GatewayAppRule appRule) throws Exception {
        return postEidasAuthnRequest(buildAuthnRequest(), appRule).getCookies().get("gateway-session");
    }

    @AfterAll
    public void tearDown() {
        this.killTestRedis();
    }

}