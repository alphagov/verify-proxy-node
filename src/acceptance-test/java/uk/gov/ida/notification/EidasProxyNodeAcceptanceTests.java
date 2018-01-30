package uk.gov.ida.notification;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class EidasProxyNodeAcceptanceTests {
    private static final String SAML_FORM = "saml-form";
    private static final String SUBMIT_BUTTON = "submit";

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

            assertEquals(successPage.getBaseURL().toString(), connectorNodeResponseUrl());
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

    private String connectorNodeResponseUrl() throws URISyntaxException {
        return getEnv("CONNECTOR_NODE_URL", proxyNodeBase("/connector-node/eidas-authn-response"));
    }

    private String proxyNodeBase(String path) throws URISyntaxException {
        String proxyNodeUrl = getEnv("PROXY_NODE_URL", "http://localhost:6600");
        return new URI(proxyNodeUrl).resolve(path).toString();
    }

    private String getEnv(String envVariableName, String defaultValue) {
        return System.getenv().getOrDefault(envVariableName, defaultValue);
    }
}
