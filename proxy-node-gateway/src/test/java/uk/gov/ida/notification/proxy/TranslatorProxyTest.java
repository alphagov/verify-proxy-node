package uk.gov.ida.notification.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.exceptions.TranslatorResponseException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.text.MessageFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.notification.shared.Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH;
import static uk.gov.ida.notification.shared.Urls.TranslatorUrls.TRANSLATOR_ROOT;

@RunWith(MockitoJUnitRunner.class)
public class TranslatorProxyTest {
    @Path(TRANSLATOR_ROOT)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestTranslatorResource {

        @Path("/happy" + TRANSLATE_HUB_RESPONSE_PATH)
        @POST
        public Response testTranslate(HubResponseTranslatorRequest request) {
            return Response.ok().entity("translated_saml_response_blob").build();
        }

        @Path("/server-error" + TRANSLATE_HUB_RESPONSE_PATH)
        @POST
        public Response testServerErrorTranslate(HubResponseTranslatorRequest request) {
            return Response.serverError().build();
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
        final HubResponseTranslatorRequest request = new HubResponseTranslatorRequest(
                "hub_response",
                "requestid",
                "eidas_request_id",
                "level_of_assurance",
                UriBuilder.fromUri("http://connector.node").build(),
                "connector_encryption_certificate"
        );

        final TranslatorProxy translatorProxy = new TranslatorProxy(
                jsonClient,
                UriBuilder.fromUri(clientRule.baseUri()).path(TRANSLATOR_ROOT).path("/happy").build()
        );

        final String samlResponse = translatorProxy.getTranslatedHubResponse(request, "session-id");

        assertThat(samlResponse).isEqualTo("translated_saml_response_blob");

        verify(jsonClient).post(
                request,
                UriBuilder.fromUri(MessageFormat.format("{0}{1}/happy{2}", clientRule.baseUri(), TRANSLATOR_ROOT, TRANSLATE_HUB_RESPONSE_PATH)).build(),
                String.class
        );
    }

    @Test
    public void shouldThrowTranslatorResponseExceptionWhenErrorPostingToTranslator() {
        final HubResponseTranslatorRequest request = new HubResponseTranslatorRequest(
                "hub_response",
                "requestid",
                "eidas_request_id",
                "level_of_assurance",
                UriBuilder.fromUri("http://connector.node").build(),
                "connector_encryption_certificate"
        );

        final TranslatorProxy translatorProxy = new TranslatorProxy(
                jsonClient,
                UriBuilder.fromUri(clientRule.baseUri()).path(TRANSLATOR_ROOT).path("/server-error").build()
        );

        assertThatThrownBy(() -> translatorProxy.getTranslatedHubResponse(request, "session-id"))
                .isInstanceOfSatisfying(TranslatorResponseException.class, e ->
                        assertThat(e.getCause()).hasMessageStartingWith(MessageFormat.format(
                                "Exception of type [REMOTE_SERVER_ERROR] whilst contacting uri: {0}{1}/server-error{2}",
                                clientRule.baseUri().toString(), TRANSLATOR_ROOT, TRANSLATE_HUB_RESPONSE_PATH)));
    }
}
