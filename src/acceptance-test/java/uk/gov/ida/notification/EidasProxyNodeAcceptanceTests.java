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
            HtmlPage countryServicePage = webClient.getPage(serviceProviderBase("/"));
            HtmlPage eidasAuthnRequestPage = submitCountrySelections(countryServicePage);
            HtmlPage proxyNodeToHubPage = submitCefRefSamlForm(eidasAuthnRequestPage);
            HtmlPage idpLoginPage = submitSamlForm(proxyNodeToHubPage);
            HtmlPage idpConsentPage = loginAtIDP(idpLoginPage);
            HtmlPage proxyNodeToConnectorNodePage = consentAtIDP(idpConsentPage);
            HtmlPage connectorNodePage = submitSamlForm(proxyNodeToConnectorNodePage);
            HtmlPage serviceProviderResponsePage = submitCefRefSamlForm(connectorNodePage);

            String pageContent = serviceProviderResponsePage.asText();
            assertEquals(serviceProviderResponsePage.getBaseURL().toString(), serviceProviderBase("/populateReturnPage"));
            assertThat(pageContent, containsString("Jack Cornelius"));
            assertThat(pageContent, containsString("Bauer"));
            assertThat(pageContent, containsString("1984-02-29"));
        }
    }

    private HtmlPage submitCountrySelections(HtmlPage countryServicePage) throws IOException, URISyntaxException {
        HtmlForm cefRefForm = countryServicePage.getForms().get(0);
        cefRefForm.getInputByName("nodeMetadataUrl").setValueAttribute(connectorNodeMetadataUrl("/ConnectorResponderMetadata"));
        cefRefForm.getSelectByName("citizenEidas").setSelectedAttribute(citizenCountry(), true);
        cefRefForm.getSelectByName("eidasloa").setSelectedAttribute("http://eidas.europa.eu/LoA/substantial", true);
        return countryServicePage.getElementById("submit_tab2").click();
    }

    private String citizenCountry() {
        return getEnv("CITIZEN_COUNTRY", "UK2");
    }

    private HtmlPage loginAtIDP(HtmlPage idpLogin) throws IOException {
        HtmlForm loginForm = idpLogin.getForms().get(0);
        loginForm.getInputByName("username").setValueAttribute("stub-idp-demo");
        loginForm.getInputByName("password").setValueAttribute("bar");
        return loginForm.getInputByValue("SignIn").click();
    }

    private HtmlPage consentAtIDP(HtmlPage idpConsent) throws IOException {
        HtmlForm consentForm = idpConsent.getForms().get(0);
        return consentForm.getInputByValue("I Agree").click();
    }

    private HtmlPage submitSamlForm(HtmlPage testSamlPage) throws IOException {
        HtmlForm authnRequestForm = testSamlPage.getFormByName(SAML_FORM);
        return authnRequestForm.getInputByName(SUBMIT_BUTTON).click();
    }

    private HtmlPage submitCefRefSamlForm(HtmlPage cefRefSamlPage) throws IOException {
        return cefRefSamlPage.getElementById("submit_saml").click();
    }

    private String connectorNodeMetadataUrl(String path) throws URISyntaxException {
        String connectorNodeUrl = getEnv("CONNECTOR_NODE_URL", "http://connector-node:8080");
        return new URI(connectorNodeUrl).resolve(path).toString();
    }

    private String proxyNodeBase(String path) throws URISyntaxException {
        String proxyNodeUrl = getEnv("PROXY_NODE_URL", "http://localhost:56016");
        return new URI(proxyNodeUrl).resolve(path).toString();
    }

    private String serviceProviderBase(String path) throws URISyntaxException {
        String proxyNodeUrl = getEnv("SERVICE_PROVIDER_URL", "http://localhost:56000");
        return new URI(proxyNodeUrl).resolve(path).toString();
    }

    private String getEnv(String envVariableName, String defaultValue) {
        return System.getenv().getOrDefault(envVariableName, defaultValue);
    }
}
