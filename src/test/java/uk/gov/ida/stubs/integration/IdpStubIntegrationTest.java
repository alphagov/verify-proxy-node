package uk.gov.ida.stubs.integration;

import io.dropwizard.client.JerseyClientBuilder;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.notification.integration.ProxyNodeAppRule;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static uk.gov.ida.notification.helpers.FileHelpers.readFileAsString;

public class IdpStubIntegrationTest {

    @ClassRule
    public static ProxyNodeAppRule proxyNodeAppRule = new ProxyNodeAppRule();

    @Test
    public void shouldHandleAuthnRequestPost() throws IOException, ParserConfigurationException {

        String hubAuthnRequest = "any input text";

        Response response = postToIdpStub(hubAuthnRequest);

        assertEquals(HttpStatus.OK_200 , response.getStatus());
        String responseAsText = response.readEntity(String.class);
        String expectedResponse = buildExpectedIdpResponse();
        assertThat(responseAsText, containsString(expectedResponse));
    }

    private Response postToIdpStub(String hubAuthnRequest) {
        String idpStubRequestUrl = "http://localhost:%d/stub-idp/request";
        return new JerseyClientBuilder(proxyNodeAppRule.getEnvironment())
                    .build("test-client")
                    .target(
                        String.format(idpStubRequestUrl, proxyNodeAppRule.getLocalPort()))
                    .request()
                    .post(Entity.text(hubAuthnRequest));
    }

    private String buildExpectedIdpResponse() throws IOException {
        return Base64.encodeAsString(readFileAsString("verify_idp_response.xml"));
    }
}
