package uk.gov.ida.notification.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.exceptions.TranslatorResponseException;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.ProxyNodeJsonClient;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
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

        public static MultivaluedMap<String, String> headers;
        @POST
        @Path("/test-journey-id-header" + TRANSLATE_HUB_RESPONSE_PATH)
        public Response testJourneyIdHeader(HubResponseTranslatorRequest hubResponseTranslatorRequest, @Context HttpHeaders headers) {
            TestTranslatorResource.headers = headers.getRequestHeaders();
            return Response.ok().entity("translated_saml_response_blob").build();
        }
    }

    private static final String JOURNEY_ID = "this_is_not_a_uuid";

    @ClassRule
    public static final DropwizardClientRule clientRule = new DropwizardClientRule(new TestTranslatorResource());

    @Spy
    ProxyNodeJsonClient jsonClient = new ProxyNodeJsonClient(
            new ErrorHandlingClient(ClientBuilder.newClient()),
            new JsonResponseProcessor(new ObjectMapper()),
            new IstioHeaderStorage()
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

    @Test
    public void shouldSendJourneyIdInHeaders() {

        MDC.put(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name(), JOURNEY_ID);

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
                UriBuilder.fromUri(clientRule.baseUri()).path(TRANSLATOR_ROOT).path("/test-journey-id-header").build()
        );

        translatorProxy.getTranslatedHubResponse(request, "session-id");

        assertThat(TranslatorProxyTest.TestTranslatorResource.headers.getFirst(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name())).isEqualTo(JOURNEY_ID);
    }
}
