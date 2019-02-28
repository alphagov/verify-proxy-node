package uk.gov.ida.notification.apprule;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
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
import uk.gov.ida.notification.exceptions.mappers.EidasSamlParserResponseExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.VspGenerateAuthnRequestResponseExceptionMapper;
import uk.gov.ida.notification.helpers.HtmlHelpers;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

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

    @Rule
    public GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response")
    );

    @Rule
    public GatewayAppRule proxyNodeEspServerErrorAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientServerErrorRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response")
    );

    @Rule
    public GatewayAppRule proxyNodeEspClientErrorAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientClientErrorRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response")
    );

    @Rule
    public GatewayAppRule proxyNodeVspServerErrorAppRule = new GatewayAppRule(
        ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
        ConfigOverride.config("verifyServiceProviderService.url", vspClientServerErrorRule.baseUri().toString()),
        ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response")
    );

    @Mock
    private Handler logHandler;

    @Captor
    private ArgumentCaptor<LogRecord> captorLoggingEvent;

    @Test
    public void bindingsReturnHubAuthnRequestForm() throws Throwable {
        assertGoodRequest(buildAuthnRequest());
    }

    @Test
    public void serverErrorResponseFromEspLogsAndReturns500() throws Exception {
        Logger logger = Logger.getLogger(EidasSamlParserResponseExceptionMapper.class.getName());
        logger.addHandler(logHandler);

        Response response = postEidasAuthnRequest(
            buildAuthnRequest(),
            proxyNodeEspServerErrorAppRule
        );

        assertEquals(500, response.getStatus());
        HtmlHelpers.assertXPath(
            getHtmlStringFromResponse(response),
            "//div[@class='issues'][text()='Something went wrong with the ESP']"
        );

        verify(logHandler).publish(captorLoggingEvent.capture());
        assertThat(captorLoggingEvent.getValue().getLevel()).isEqualTo(WARNING);
        assertThat(captorLoggingEvent.getValue().getMessage())
            .matches("Exception calling eidas-saml-parser for session '.*': Exception of type \\[REMOTE_SERVER_ERROR\\] whilst contacting uri: .*\n.*");
    }

    @Test
    public void clientErrorResponseFromEspLogsAndReturns400() throws Exception {
        Logger logger = Logger.getLogger(EidasSamlParserResponseExceptionMapper.class.getName());
        logger.addHandler(logHandler);

        AuthnRequest request = buildAuthnRequest();
        Response response = postEidasAuthnRequest(request, proxyNodeEspClientErrorAppRule);

        assertEquals(400, response.getStatus());
        HtmlHelpers.assertXPath(
            getHtmlStringFromResponse(response),
            "//div[@class='issues'][text()='Something went wrong with the ESP']"
        );

        verify(logHandler).publish(captorLoggingEvent.capture());
        assertThat(captorLoggingEvent.getValue().getLevel()).isEqualTo(WARNING);
        assertThat(captorLoggingEvent.getValue().getMessage())
            .matches("Exception calling eidas-saml-parser for session '.*': Exception of type \\[CLIENT_ERROR\\] whilst contacting uri: .*\n.*");
    }

    @Test
    public void serverErrorResponseFromVspLogsAndReturns500() throws Exception {
        Logger logger = Logger.getLogger(VspGenerateAuthnRequestResponseExceptionMapper.class.getName());
        logger.addHandler(logHandler);

        Response response = postEidasAuthnRequest(
            buildAuthnRequest(),
            proxyNodeVspServerErrorAppRule
        );

        assertEquals(500, response.getStatus());
        HtmlHelpers.assertXPath(
            getHtmlStringFromResponse(response),
            "//div[@class='issues'][text()='Something went wrong with the VSP']"
        );

        verify(logHandler).publish(captorLoggingEvent.capture());
        assertThat(captorLoggingEvent.getValue().getLevel()).isEqualTo(WARNING);
        assertThat(captorLoggingEvent.getValue().getMessage())
            .matches("Exception calling verify-service-provider for session '.*': Exception of type \\[REMOTE_SERVER_ERROR\\] whilst contacting uri:.*\n.*");
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
}