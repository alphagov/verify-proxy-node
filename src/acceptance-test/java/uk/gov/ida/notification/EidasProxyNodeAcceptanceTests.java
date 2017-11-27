package uk.gov.ida.notification;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.notification.helpers.FileHelpers;
import uk.gov.ida.notification.integration.ProxyNodeAppRule;
import uk.gov.ida.notification.saml.SamlMessageType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EidasProxyNodeAcceptanceTests {
    private static final String PROXY_NODE_HUB_RESPONSE = "/SAML2/Response/POST";
    private static final String SAML_FORM = "saml-form";
    private static final String SUBMIT_BUTTON = "submit";
    private static final String FIXED_ID = "_0ac6a8af9fed04143875c565d97aed6b";
    private static final String CONNECTOR_NODE_REQUEST = "/connector-node/eidas-authn-request";
    private static final String CONNECTOR_NODE_RESPONSE = "/connector-node/eidas-authn-response";
    private static final String HUB_URL = "/stub-idp/request";

    @ClassRule
    public static ProxyNodeAppRule proxyNodeAppRule = new ProxyNodeAppRule();

    @Test
    public void shouldHandleEidasAuthnRequest() throws Exception {
        try (final WebClient webClient = new WebClient()) {
            HtmlPage testSamlPage = webClient.getPage(connectorNodeRequestUrl());
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
            hubSamlShouldBeAsExpected(hubSamlResponse);

            HtmlPage eIdasSamlResponsePage = hubSamlResponseForm.getInputByName(SUBMIT_BUTTON).click();
            HtmlForm eIdasSamlResponseForm = eIdasSamlResponsePage.getFormByName(SAML_FORM);
            HtmlInput eIdasSamlResponse = eIdasSamlResponseForm.getInputByName(SamlMessageType.SAML_RESPONSE);
            assertEquals(connectorNodeResponseUrl(), eIdasSamlResponseForm.getActionAttribute());
            eidasResponseShouldBeAsExpected(eIdasSamlResponse);
        }
    }

    private void hubSamlShouldBeAsExpected(HtmlInput hubResponse) throws IOException {
        String expectedIdpResponseSaml = buildExpectedHubResponseSaml();
        String hubSaml = hubResponse.getAttributes().getNamedItem("value").getNodeValue();
        assertEquals(expectedIdpResponseSaml, hubSaml);
    }

    private void eidasResponseShouldBeAsExpected(HtmlInput eidasResponse) throws IOException {
        String expectedEidasResponseSaml = buildExpectedEidasResponseSaml();
        String eIdasSaml = eidasResponse.getAttributes().getNamedItem("value").getNodeValue();
        assertEquals(expectedEidasResponseSaml, eIdasSaml);
    }

    private String buildExpectedHubResponseSaml() throws IOException {
        String expectedIdpSamlFileName = "verify_idp_response.xml";
        String hubSaml = FileHelpers.readFileAsString(expectedIdpSamlFileName);
        return Base64.encodeAsString(hubSaml);
    }

    private String buildExpectedEidasResponseSaml() throws IOException {
        String expectedEidasSamlFileName = "eidas_idp_response.xml";
        String eidasSaml = FileHelpers.readFileAsString(expectedEidasSamlFileName);
        return Base64.encodeAsString(eidasSaml);
    }

    private String connectorNodeRequestUrl() throws URISyntaxException {
        return proxyNodeBase(CONNECTOR_NODE_REQUEST);
    }

    private String connectorNodeResponseUrl() throws URISyntaxException {
        return proxyNodeBase(CONNECTOR_NODE_RESPONSE);
    }

    private String hubUrl() throws URISyntaxException {
        String hubUrlDefaultValue = proxyNodeBase(HUB_URL);
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
