package uk.gov.ida.notification.shared.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
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
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VerifyServiceProviderTranslationRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspScenario;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class VspProxyTranslateResponseTest {

    @Spy
    private static JsonClient jsonClient = createJsonClient();

    @Path("/translate-response")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestVSPResourceIdentityVerified {

        @POST
        public TranslatedHubResponse testGetTranslatedHubResponse(VerifyServiceProviderTranslationRequest request) {
            return TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerified();
        }
    }

    @Path("/translate-response")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestVSPResourceIdentityAuthnFailed {

        @POST
        public TranslatedHubResponse testGetTranslatedHubResponse(VerifyServiceProviderTranslationRequest request) {
            return TranslatedHubResponseBuilder.buildTranslatedHubResponseAuthenticationFailed();
        }
    }

    @Path("/translate-response")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestVSPServerErrorResource {

        @POST
        public Response testGenerate(AuthnRequestGenerationBody authnRequestGenerationBody) {
            return Response.serverError().build();
        }
    }

    @Path("/translate-response")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestVSPClientErrorResource {

        @POST
        public Response testGenerate(AuthnRequestGenerationBody authnRequestGenerationBody) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @ClassRule
    public static final DropwizardClientRule testVspIdentityVerifiedClientRule = new DropwizardClientRule(new TestVSPResourceIdentityVerified());

    @ClassRule
    public static final DropwizardClientRule testVspAuthnFailedClientRule = new DropwizardClientRule(new TestVSPResourceIdentityAuthnFailed());

    @ClassRule
    public static final DropwizardClientRule testVspServerErrorClientRule = new DropwizardClientRule(new TestVSPServerErrorResource());

    @ClassRule
    public static final DropwizardClientRule testVspClientErrorClientRule = new DropwizardClientRule(new TestVSPClientErrorResource());


    @Test
    public void shouldReturnTranslatedHubResponseAuthnFailed() {

        VerifyServiceProviderProxy vspProxy = new VerifyServiceProviderProxy(jsonClient, testVspAuthnFailedClientRule.baseUri());

        TranslatedHubResponse response =
                vspProxy.getTranslatedHubResponse(
                        new VerifyServiceProviderTranslationRequest(null, "_1234", "LEVEL_2")
                );

        assertThat(VspScenario.AUTHENTICATION_FAILED).isEqualTo(response.getScenario());
        assertThat("123456").isEqualTo(response.getPid());
        assertThat(VspLevelOfAssurance.LEVEL_2).isEqualTo(response.getLevelOfAssurance());
        assertThat(response.getAttributes()).isNull();

        Mockito.verify(jsonClient).post(
                Mockito.argThat((VerifyServiceProviderTranslationRequest request) -> request.getLevelOfAssurance().equals("LEVEL_2")),
                eq(UriBuilder.fromUri(String.format("%s/translate-response", testVspAuthnFailedClientRule.baseUri())).build()),
                eq(TranslatedHubResponse.class)
        );
    }

    @Test
    public void shouldReturnTranslatedHubResponse() {

        VerifyServiceProviderProxy vspProxy = new VerifyServiceProviderProxy(jsonClient, testVspIdentityVerifiedClientRule.baseUri());

        TranslatedHubResponse response =
                vspProxy.getTranslatedHubResponse(
                        new VerifyServiceProviderTranslationRequest("SAMLResponse1234", "_1234", "LEVEL_2")
                );

        assertThat(VspScenario.IDENTITY_VERIFIED).isEqualTo(response.getScenario());
        assertThat("123456").isEqualTo(response.getPid());
        assertThat(VspLevelOfAssurance.LEVEL_2).isEqualTo(response.getLevelOfAssurance());
    }

    @Test
    public void shouldThrowApplicationExceptionOnClientError() {
        VerifyServiceProviderProxy vspProxy = new VerifyServiceProviderProxy(jsonClient, testVspClientErrorClientRule.baseUri());

        assertThatThrownBy(() -> vspProxy.getTranslatedHubResponse(
                new VerifyServiceProviderTranslationRequest("SAMLResponse1234", "_1234", "LEVEL_2")));
    }

    @Test
    public void shouldThrowApplicationExceptionOnServerError() {
        VerifyServiceProviderProxy vspProxy = new VerifyServiceProviderProxy(jsonClient, testVspServerErrorClientRule.baseUri());

        assertThatThrownBy(() -> vspProxy.getTranslatedHubResponse(
                new VerifyServiceProviderTranslationRequest("SAMLResponse1234", "_1234", "LEVEL_2")));
    }

    private static JsonClient createJsonClient() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return new JsonClient(
                new ErrorHandlingClient(
                        ClientBuilder
                                .newBuilder()
                                .register(new JacksonJsonProvider(objectMapper))
                                .build()
                ),
                new JsonResponseProcessor(objectMapper)
        );
    }
}
