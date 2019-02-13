package uk.gov.ida.notification.proxy;

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
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TranslatorProxyTest {
    @Path("/translate-hub-response")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestTranslatorResource {

        @POST
        public Response testTranslate(HubResponseTranslatorRequest request) {
            System.out.println("What?");
            return Response.ok().entity("translated_saml_response_blob").build();
        }
    }

    @ClassRule
    public static final DropwizardClientRule clientRule = new DropwizardClientRule(new TestTranslatorResource());

    @Spy
    JsonClient jsonClient = new JsonClient(
        new ErrorHandlingClient(ClientBuilder.newClient()),
        new JsonResponseProcessor(new ObjectMapper())
    );

    @Test
    public void shouldAcceptHubResponseTranslatorRequestAndReturnString() {
        HubResponseTranslatorRequest request = new HubResponseTranslatorRequest(
            "hub_response",
            "requestid",
            "eidas_request_id",
            "level_of_assurance",
            UriBuilder.fromUri("http://connector.node").build(),
            "connector_encryption_certificate"
        );
        TranslatorProxy translatorProxy = new TranslatorProxy(
            jsonClient,
            UriBuilder.fromUri(clientRule.baseUri()).path("/translate-hub-response").build());

        String samlResponse = translatorProxy.getTranslatedResponse(request);

        assertEquals("translated_saml_response_blob", samlResponse);
        Mockito.verify(jsonClient).post(
            request,
            UriBuilder.fromUri(String.format("%s/translate-hub-response", clientRule.baseUri())).build(),
            String.class
        );
    }
}
