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
import static org.junit.Assert.assertTrue;

public class EidasProxyNodeAcceptanceTests {
    public static final String PROXY_NODE_HUB_RESPONSE = "/SAML2/Response/POST";
    private static final String SAML_FORM = "saml-form";
    private static final String SUBMIT_BUTTON = "submit";
    private static final String FIXED_ID = "_0ac6a8af9fed04143875c565d97aed6b";
    private static final String CONNECTOR_NODE = "/connector-node/eidas-authn-request";
    private static final String IDP_URL = "/stub-idp/request";

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
            assertEquals(idpUrl(), verifyAuthnRequestForm.getActionAttribute());
            assertNotNull(verifyAuthnRequestSAML);

            HtmlPage idpSamlResponsePage = verifyAuthnRequestForm.getInputByName(SUBMIT_BUTTON).click();
            HtmlForm idpSamlResponseForm = idpSamlResponsePage.getFormByName(SAML_FORM);
            HtmlInput idpSamlResponse = idpSamlResponseForm.getInputByName(SamlMessageType.SAML_RESPONSE);
            assertEquals(proxyNodeBase(PROXY_NODE_HUB_RESPONSE), idpSamlResponseForm.getActionAttribute());
            idpSamlShouldBeAsExpected(idpSamlResponse);

            HtmlPage eIdasSamlResponsePage = idpSamlResponseForm.getInputByName(SUBMIT_BUTTON).click();
            HtmlForm eIdasSamlResponseForm = eIdasSamlResponsePage.getFormByName(SAML_FORM);
            HtmlInput eIdasSamlResponse = eIdasSamlResponseForm.getInputByName(SamlMessageType.SAML_RESPONSE);
            assertEquals(connectorNodeUrl(), eIdasSamlResponseForm.getActionAttribute());
            eidasResponseShouldBeAsExpected(eIdasSamlResponse);
        }
    }

    private void idpSamlShouldBeAsExpected(HtmlInput idpSamlResponse) throws IOException {
        String expectedIdpResponseSaml = buildExpectedIdpResponseSaml();
        String idpSaml = idpSamlResponse.getAttributes().getNamedItem("value").getNodeValue();
        assertEquals(expectedIdpResponseSaml, idpSaml);
    }

    private void eidasResponseShouldBeAsExpected(HtmlInput eidasResponse) throws IOException {
        String expectedEidasResponseSaml = buildExpectedEidasResponseSaml();
        String eIdasSaml = eidasResponse.getAttributes().getNamedItem("value").getNodeValue();
        assertEquals(expectedEidasResponseSaml, eIdasSaml);
    }

    private String buildExpectedIdpResponseSaml() throws IOException {
        String expectedIdpSamlFileName = "verify_idp_response.xml";
        String idpSaml = FileHelpers.readFileAsString(expectedIdpSamlFileName);
        return Base64.encodeAsString(idpSaml);
    }

    private String buildExpectedEidasResponseSaml() throws IOException {
        String expectedEidasSamlFileName = "eidas_idp_response.xml";
        String eidasSaml = FileHelpers.readFileAsString(expectedEidasSamlFileName);
        return Base64.encodeAsString(eidasSaml);
    }

    private String connectorNodeUrl() throws URISyntaxException {
        return proxyNodeBase(CONNECTOR_NODE);
    }

    private String idpUrl() throws URISyntaxException {
        String hubUrlDefaultValue = proxyNodeBase(IDP_URL);
        return getEnvVariableOrDefault("IDP_URL", hubUrlDefaultValue);
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
