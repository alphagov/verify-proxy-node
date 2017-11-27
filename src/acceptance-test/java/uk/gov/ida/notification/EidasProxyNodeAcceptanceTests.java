package uk.gov.ida.notification;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.ClassRule;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class EidasProxyNodeAcceptanceTests {
    private static final String SAML_FORM = "saml-form";
    private static final String SUBMIT_BUTTON = "submit";

    @ClassRule
    public static EidasProxyNodeAppRule proxyNodeAppRule = new EidasProxyNodeAppRule();

    @Test
    public void shouldHandleEidasAuthnRequest() throws Exception {
        try (final WebClient webClient = new WebClient()) {
            HtmlPage testSamlPage = webClient.getPage(connectorNodeUrl());

            HtmlPage verifyAuthnRequestPage = submitSamlForm(testSamlPage);
            HtmlPage idpSamlResponsePage = submitSamlForm(verifyAuthnRequestPage);
            HtmlPage eidasSamlResponsePage = submitSamlForm(idpSamlResponsePage);
            HtmlPage successPage = submitSamlForm(eidasSamlResponsePage);

            String content = successPage.getBody().getTextContent();

            assertThat(content, containsString("ES/AT/02635542Y"));
            assertThat(content, containsString("http://eidas.europa.eu/LoA/substantial"));
            assertThat(content, containsString("Jack Cornelius"));
            assertThat(content, containsString("Bauer"));
            assertThat(content, containsString("1984-02-29"));
        }
    }

    private HtmlPage submitSamlForm(HtmlPage testSamlPage) throws java.io.IOException {
        HtmlForm authnRequestForm = testSamlPage.getFormByName(SAML_FORM);
        return authnRequestForm.getInputByName(SUBMIT_BUTTON).click();
    }

    private String connectorNodeUrl() throws URISyntaxException {
        return getEnvVariableOrDefault("CONNECTOR_NODE_URL", proxyNodeBase("/connector-node/eidas-authn-request"));
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
