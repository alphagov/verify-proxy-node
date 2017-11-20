package uk.gov.ida.notification.integration;

import io.dropwizard.client.JerseyClientBuilder;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static uk.gov.ida.notification.helpers.FileHelpers.readFileAsString;

public class EidasProxyNodeIntegrationTest {
    @Rule
    public final ProxyNodeAppRule proxyNodeTestSupport = new ProxyNodeAppRule();

    @Test
    public void shouldHandleAuthnRequestPost() throws InterruptedException, IOException {
        String encodedAuthnRequest = Base64.encodeAsString(readFileAsString("eidas_authn_request.xml"));
        Response response = postAuthnRequest(encodedAuthnRequest);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldHandleAuthnRequestRedirect() throws InterruptedException, IOException {
        String encodedAuthnRequest = Base64.encodeAsString(readFileAsString("eidas_authn_request.xml"));
        Response response = redirectAuthnRequest(encodedAuthnRequest);
        assertEquals(200, response.getStatus());
    }

    private Response postAuthnRequest(String encodedAuthnRequest) {
        MultivaluedHashMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("SAMLRequest", encodedAuthnRequest);

        return getClient().target(
                String.format("http://localhost:%d/SAML2/SSO/POST", proxyNodeTestSupport.getLocalPort()))
                .request()
                .post(Entity.form(formData));
    }

    private Response redirectAuthnRequest(String encodedAuthnRequest) {
        return getClient().target(
                String.format("http://localhost:%d/SAML2/SSO/Redirect", proxyNodeTestSupport.getLocalPort()))
                .queryParam("SAMLRequest", encodedAuthnRequest)
                .request()
                .get();
    }

    private Client getClient() {
        return new JerseyClientBuilder(proxyNodeTestSupport.getEnvironment())
                .withProperty("timeout", "20s")
                .withProperty("connectionTimeout", "20s")
                .build("test-client");
    }
}
