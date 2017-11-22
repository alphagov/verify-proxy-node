package uk.gov.ida.notification;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.notification.integration.ProxyNodeAppRule;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.views.SamlFormView;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EidasProxyNodeAcceptanceTests {
    private static final String SAML_FORM = "saml-form";
    private static final String SUBMIT_BUTTON = "submit";
    private static final String CONNECTOR_NODE = "/connector-node/eidas-authn-request";
    private static final String IDP_URL = "/stub-idp/request";

    public static final String PROXY_NODE_HUB_RESPONSE = "/SAML2/SSO/idp-response";
    @ClassRule
    public static ProxyNodeAppRule proxyNodeAppRule = new ProxyNodeAppRule();

    @Test
    public void shouldHandleEidasAuthnRequest() throws Exception {
        try (final WebClient webClient = new WebClient()) {
            HtmlPage testSamlPage = webClient.getPage(connectorNodeUrl());
            HtmlForm authnRequestForm = testSamlPage.getFormByName(SAML_FORM);

            HtmlPage verifyAuthnRequestPage = authnRequestForm.getInputByName(SUBMIT_BUTTON).click();
            HtmlForm verifyAuthnRequestForm = verifyAuthnRequestPage.getFormByName(SAML_FORM);
            HtmlInput verifyAuthnRequestSAML = verifyAuthnRequestForm.getInputByName(SamlMessageType.SAML_REQUEST);
            assertEquals(hubUrl(), verifyAuthnRequestForm.getActionAttribute());
            assertNotNull(verifyAuthnRequestSAML);

            HtmlPage hubSamlResponsePage = verifyAuthnRequestForm.getInputByName(SUBMIT_BUTTON).click();
            HtmlForm hubSamlResponseForm = hubSamlResponsePage.getFormByName(SAML_FORM);
            HtmlInput hubSamlResponse = hubSamlResponseForm.getInputByName(SamlMessageType.SAML_RESPONSE);
            assertEquals(proxyNodeBase(PROXY_NODE_HUB_RESPONSE), hubSamlResponseForm.getActionAttribute());
            assertNotNull(hubSamlResponse);

            HtmlPage eIdasSamlResponsePage = hubSamlResponseForm.getInputByName(SUBMIT_BUTTON).click();
            HtmlForm eIdasSamlResponseForm = eIdasSamlResponsePage.getFormByName(SAML_FORM);
            HtmlInput eIdasSamlResponse = eIdasSamlResponseForm.getInputByName(SamlMessageType.SAML_RESPONSE);
            assertEquals(connectorNodeUrl(), eIdasSamlResponseForm.getActionAttribute());
            assertNotNull(eIdasSamlResponse);
        }
    }

    private String connectorNodeUrl() throws URISyntaxException {
        return proxyNodeBase(CONNECTOR_NODE);
    }

    private String hubUrl() throws URISyntaxException {
        String hubUrlDefaultValue = proxyNodeBase(IDP_URL) ;
        return getEnvVariableOrDefault("HUB_URL", hubUrlDefaultValue);
    }

    private String proxyNodeBase(String path) throws URISyntaxException {
        String proxyNodeUrlDefaultValue = "http://localhost:" + proxyNodeAppRule.getLocalPort();
        String proxyNodeUrl = getEnvVariableOrDefault("PROXY_NODE_URL", proxyNodeUrlDefaultValue);
        return new URI(proxyNodeUrl).resolve(path).toString();
    }

    private String getEnvVariableOrDefault(String envVariableName, String defaultValue) {
        return System.getenv().getOrDefault(envVariableName, defaultValue);
    }
}
