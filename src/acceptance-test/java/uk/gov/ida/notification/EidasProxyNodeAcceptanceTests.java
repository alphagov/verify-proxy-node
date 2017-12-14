package uk.gov.ida.notification;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.junit.Test;

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
