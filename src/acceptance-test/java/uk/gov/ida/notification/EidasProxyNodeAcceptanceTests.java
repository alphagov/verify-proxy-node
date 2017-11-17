package uk.gov.ida.notification;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.notification.integration.ProxyNodeAppRule;
import uk.gov.ida.notification.views.SamlFormView;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EidasProxyNodeAcceptanceTests {
    private static final String SAML_FORM = "saml-form";
    private static final String SUBMIT_BUTTON = "submit";
    private static final String CONNECTOR_NODE = "/test-saml/eidas-authn-request";

    @ClassRule
    public static ProxyNodeAppRule proxyNodeAppRule = new ProxyNodeAppRule();

    @Test
    public void shouldHandleEidasAuthnRequest() throws Exception {
        try (final WebClient webClient = new WebClient()) {
            HtmlPage testSamlPage = webClient.getPage(proxyNodeUrl(CONNECTOR_NODE));
            HtmlForm authnRequestForm = testSamlPage.getFormByName(SAML_FORM);
            HtmlPage nextPage = authnRequestForm.getInputByName(SUBMIT_BUTTON).click();

            authnRequestForm = nextPage.getFormByName(SAML_FORM);
            HtmlInput samlRequest = authnRequestForm.getInputByName(SamlFormView.SAML_REQUEST);

            assertEquals(hubUrl(), authnRequestForm.getActionAttribute());
            assertNotNull(samlRequest);
        }
    }

    private String hubUrl() {
        return System.getenv().getOrDefault(
                "HUB_URL", proxyNodeAppRule.getConfiguration().getHubUrl().toString()
        );
    }

    private String proxyNodeUrl(String path) throws URISyntaxException {
        String proxyNodeUrl = System.getenv().getOrDefault(
                "PROXY_NODE_URL", "http://localhost:"+proxyNodeAppRule.getLocalPort()
        );
        return new URI(proxyNodeUrl).resolve(path).toString();
    }
}
