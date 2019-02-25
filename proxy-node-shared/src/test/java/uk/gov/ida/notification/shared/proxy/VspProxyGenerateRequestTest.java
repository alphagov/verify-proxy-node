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
import uk.gov.ida.notification.exceptions.proxy.VspGenerateAuthnRequestResponseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class VspProxyGenerateRequestTest {

    @Path("/generate-request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestVSPResource {

        @POST
        public AuthnRequestResponse testGenerate(AuthnRequestGenerationBody authnRequestGenerationBody) {
            return new AuthnRequestResponse("saml_request", "request_id", UriBuilder.fromUri("http://sso-location.com").build());
        }
    }

    @Path("/generate-request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestVSPServerErrorResource {

        @POST
        public Response testGenerate(AuthnRequestGenerationBody authnRequestGenerationBody) {
            return Response.serverError().build();
        }
    }

    @Path("/generate-request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestVSPClientErrorResource {

        @POST
        public Response testGenerate(AuthnRequestGenerationBody authnRequestGenerationBody) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @ClassRule
    public static final DropwizardClientRule testVspClientRule = new DropwizardClientRule(new TestVSPResource());

    @ClassRule
    public static final DropwizardClientRule testVspServerErrorClientRule = new DropwizardClientRule(new TestVSPServerErrorResource());

    @ClassRule
    public static final DropwizardClientRule testVspClientErrorClientRule = new DropwizardClientRule(new TestVSPClientErrorResource());

    @Spy
    JsonClient jsonClient = new JsonClient(
            new ErrorHandlingClient(ClientBuilder.newClient()),
            new JsonResponseProcessor(new ObjectMapper())
    );

    @Test
    public void generateAuthnRequestShouldReturnVSPAuthnRequestResponse() {
        VerifyServiceProviderProxy vspProxy = new VerifyServiceProviderProxy(jsonClient, testVspClientRule.baseUri());

        AuthnRequestResponse response = vspProxy.generateAuthnRequest("session-id");

        assertEquals("saml_request", response.getSamlRequest());
        assertEquals("request_id", response.getRequestId());
        assertEquals(UriBuilder.fromUri("http://sso-location.com").build(), response.getSsoLocation());

        Mockito.verify(jsonClient).post(
                Mockito.argThat((AuthnRequestGenerationBody request) -> request.getLevelOfAssurance().equals("LEVEL_2")),
                eq(UriBuilder.fromUri(String.format("%s/generate-request", testVspClientRule.baseUri())).build()),
                eq(AuthnRequestResponse.class)
        );
    }

    @Test
    public void shouldThrowVerifyServiceProviderResponseExceptionOnServerError() {
        VerifyServiceProviderProxy vspProxy = new VerifyServiceProviderProxy(jsonClient, testVspServerErrorClientRule.baseUri());

        try {
            AuthnRequestResponse response = vspProxy.generateAuthnRequest("session-id");
            fail("Expected exception not thrown");
        } catch (VspGenerateAuthnRequestResponseException e) {
            assertThat(e.getCause().getMessage())
                    .startsWith(
                            String.format(
                                    "Exception of type [REMOTE_SERVER_ERROR] whilst contacting uri: %s/generate-request",
                                    testVspServerErrorClientRule.baseUri().toString()
                            )
                    );
            assertEquals("session-id", e.getSessionId());
        }
    }

    @Test
    public void shouldThrowVerifyServiceProviderResponseExceptionOnClientError() {
        VerifyServiceProviderProxy vspProxy = new VerifyServiceProviderProxy(jsonClient, testVspClientErrorClientRule.baseUri());

        try {
            AuthnRequestResponse response = vspProxy.generateAuthnRequest("session-id");
            fail("Expected exception not thrown");
        } catch (VspGenerateAuthnRequestResponseException e) {
            assertThat(e.getCause().getMessage())
                    .startsWith(
                            String.format(
                                    "Exception of type [CLIENT_ERROR] whilst contacting uri: %s/generate-request",
                                    testVspClientErrorClientRule.baseUri().toString()
                            )
                    );
            assertEquals("session-id", e.getSessionId());
        }
    }
}