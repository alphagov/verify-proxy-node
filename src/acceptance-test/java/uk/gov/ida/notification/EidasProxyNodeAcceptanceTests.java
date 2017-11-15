package uk.gov.ida.notification;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.notification.integration.ProxyNodeAppRule;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class EidasProxyNodeAcceptanceTests {
    @ClassRule
    public static ProxyNodeAppRule proxyNodeAppRule = new ProxyNodeAppRule();

    @Test
    public void shouldHandleEidasAuthnRequest() throws Exception {
        try (final WebClient webClient = new WebClient()) {
            HtmlPage testSamlPage = webClient.getPage(proxyNodeUrl("/test-saml/eidas-authn-request"));
            HtmlForm authnRequestForm = testSamlPage.getFormByName("saml-form");
            HtmlPage authnRequestReceivedPage = authnRequestForm.getInputByName("submit").click();

            assertEquals("https://connector-node.eu", authnRequestReceivedPage.getElementById("issuer").getTextContent());
            assertEquals("https://proxy-node.uk/SAML2/SSO/POST", authnRequestReceivedPage.getElementById("destination").getTextContent());
        }
    }

    private String proxyNodeUrl(String path) throws URISyntaxException {
        String proxyNodeUrl = System.getenv().getOrDefault(
                "PROXY_NODE_URL", "http://localhost:"+proxyNodeAppRule.getLocalPort()
        );
        return new URI(proxyNodeUrl).resolve(path).toString();
    }
}
