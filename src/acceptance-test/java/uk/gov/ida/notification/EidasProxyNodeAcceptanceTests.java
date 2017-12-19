package uk.gov.ida.notification;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class EidasProxyNodeAcceptanceTests {
    private static final String SAML_FORM = "saml-form";
    private static final String SUBMIT_BUTTON = "submit";
    public static final String HUB_METADATA_ENDPOINT = "/hub-metadata/local";

    @Test
    public void shouldHubFetchMetadata() throws Exception {
        try (final WebClient webClient = new WebClient()) {
            XmlPage hubMetadataPage = webClient.getPage(proxyNodeBase(HUB_METADATA_ENDPOINT));
            String content = hubMetadataPage.asXml();
            assertThat(content, containsString("EntityDescriptor"));
        }
    }

    @Test
    public void shouldHandleEidasAuthnRequest() throws Exception {
        try (final WebClient webClient = new WebClient()) {
            // Disable JS
            webClient.getOptions().setJavaScriptEnabled(false);

            // Service Start Page
            HtmlPage testSamlPage = webClient.getPage(connectorNodeUrl());

            // Submit eIDAS AuthnRequest to Proxy Node
            HtmlPage verifyAuthnRequestPage = submitSamlForm(testSamlPage);

            // Submit Verify AuthnRequest to Hub
            HtmlPage idpLoginPage = submitSamlForm(verifyAuthnRequestPage);

            // Login at Hub (IDP)
            HtmlPage idpConsentPage = loginAtIDP(idpLoginPage);
            HtmlPage idpSamlResponsePage = consentAtIDP(idpConsentPage);

            // Submit eIDAS Response to Connector Node
            HtmlPage successPage = submitSamlForm(idpSamlResponsePage);

            String content = successPage.getBody().getTextContent();

            assertThat(content, containsString("http://eidas.europa.eu/LoA/substantial"));
            assertThat(content, containsString("Jack Cornelius"));
            assertThat(content, containsString("Bauer"));
            assertThat(content, containsString("1984-02-29"));
        }
    }

    private HtmlPage loginAtIDP(HtmlPage idpLogin) throws IOException {
        HtmlForm loginForm = idpLogin.getForms().get(0);
        loginForm.getInputByName("username").setValueAttribute("stub-idp-demo");
        loginForm.getInputByName("password").setValueAttribute("bar");
        return loginForm.getInputByValue("SignIn").click();
    }

    private HtmlPage consentAtIDP(HtmlPage idpConsent) throws IOException {
        HtmlForm consentForm = idpConsent.getForms().get(0);
        HtmlPage continuePage = consentForm.getInputByValue("I Agree").click();
        return continuePage.getForms().get(0).getElementsByTagName("button").get(0).click();
    }

    private HtmlPage submitSamlForm(HtmlPage testSamlPage) throws IOException {
        HtmlForm authnRequestForm = testSamlPage.getFormByName(SAML_FORM);
        return authnRequestForm.getInputByName(SUBMIT_BUTTON).click();
    }

    private String connectorNodeUrl() throws URISyntaxException {
        return getEnv("CONNECTOR_NODE_URL", proxyNodeBase("/connector-node/eidas-authn-request"));
    }

    private String proxyNodeBase(String path) throws URISyntaxException {
        String proxyNodeUrl = getEnv("PROXY_NODE_URL", "http://localhost:6600");
        return new URI(proxyNodeUrl).resolve(path).toString();
    }

    private String getEnv(String envVariableName, String defaultValue) {
        return System.getenv().getOrDefault(envVariableName, defaultValue);
    }
}
