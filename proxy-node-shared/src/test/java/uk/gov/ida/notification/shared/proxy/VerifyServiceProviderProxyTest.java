package uk.gov.ida.notification.shared.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestGenerationBody;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class VerifyServiceProviderProxyTest {

    @Path("/generate-request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestVSPResource {

        @POST
        public AuthnRequestResponse testGenerate(AuthnRequestGenerationBody authnRequestGenerationBody) {
            return new AuthnRequestResponse("saml_request", "request_id", UriBuilder.fromUri("http://sso-location.com").build());
        }
    }

    @ClassRule
    public static final DropwizardClientRule clientRule = new DropwizardClientRule(new TestVSPResource());

    @Spy
    JsonClient jsonClient = new JsonClient(
        new ErrorHandlingClient(ClientBuilder.newClient()),
        new JsonResponseProcessor(new ObjectMapper())
    );

    @Test
    public void generateAuthnRequestShouldReturnVSPAuthnRequestResponse() {
        VerifyServiceProviderProxy vspProxy = new VerifyServiceProviderProxy(jsonClient, clientRule.baseUri());

        AuthnRequestResponse response = vspProxy.generateAuthnRequest();

        assertEquals("saml_request", response.getSamlRequest());
        assertEquals("request_id", response.getRequestId());
        assertEquals(UriBuilder.fromUri("http://sso-location.com").build(), response.getSsoLocation());

        Mockito.verify(jsonClient).post(
            Mockito.argThat((AuthnRequestGenerationBody request) -> request.getLevelOfAssurance() == "LEVEL_2"),
            eq(UriBuilder.fromUri(String.format("%s/generate-request", clientRule.baseUri())).build()),
            eq(AuthnRequestResponse.class)
        );
    }
}
