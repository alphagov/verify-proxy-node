package uk.gov.ida.notification.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.MDC;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.exceptions.EidasSamlParserResponseException;
import uk.gov.ida.notification.helpers.SelfSignedCertificateGenerator;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.ProxyNodeJsonClient;

import javax.validation.Valid;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_DESTINATION_URL;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_HUB_SAML_AUTHN_REQUEST;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_ENTITY_ID;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_REQUEST_ID;

public class EidasSamlParserProxyTest {

    private static final String UNCHAINED_PUBLIC_PEM;
    private static final String JOURNEY_ID = "this_is_not_a_uuid";
    private static final EidasSamlParserRequest eidasSamlParserRequest = new EidasSamlParserRequest(SAMPLE_HUB_SAML_AUTHN_REQUEST);

    static {
        try {
            UNCHAINED_PUBLIC_PEM = new SelfSignedCertificateGenerator("test-cn").getCertificateAsPEM();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Path("/parse")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestESPResource {

        @POST
        @Valid
        @Path("/valid")
        public EidasSamlParserResponse testValidParse(EidasSamlParserRequest eidasSamlParserRequest) {
            return new EidasSamlParserResponse(SAMPLE_REQUEST_ID, SAMPLE_ENTITY_ID, UNCHAINED_PUBLIC_PEM, SAMPLE_DESTINATION_URL);
        }

        @POST
        @Valid
        @Path("/server-error")
        public Response testServerError(EidasSamlParserRequest eidasSamlParserRequest) {
            return Response.serverError().build();
        }

        @POST
        @Valid
        @Path("/client-error")
        public Response testClientError(EidasSamlParserRequest eidasSamlParserRequest) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        static MultivaluedMap<String, String> headers;

        @POST
        @Path("/test-journey-id-header")
        public EidasSamlParserResponse testJourneyIdHeader(EidasSamlParserRequest eidasSamlParserRequest, @Context HttpHeaders headers) {
            TestESPResource.headers = headers.getRequestHeaders();
            return new EidasSamlParserResponse(SAMPLE_REQUEST_ID, SAMPLE_ENTITY_ID, UNCHAINED_PUBLIC_PEM, SAMPLE_DESTINATION_URL);
        }
    }

    @ClassRule
    public static final DropwizardClientRule clientRule = new DropwizardClientRule(new TestESPResource());


    @Test
    public void shouldReturnEidasSamlParserResponse() {
        EidasSamlParserProxy eidasSamlParserService = setUpEidasSamlParserService("/parse/valid");

        EidasSamlParserResponse response = eidasSamlParserService.parse(eidasSamlParserRequest, "session_id");

        assertThat(SAMPLE_REQUEST_ID).isEqualTo(response.getRequestId());
        assertThat(SAMPLE_ENTITY_ID).isEqualTo(response.getIssuerEntityId());
        assertThat(UNCHAINED_PUBLIC_PEM).isEqualTo(response.getConnectorEncryptionPublicCertificate());
        assertThat(SAMPLE_DESTINATION_URL).isEqualTo(response.getDestination());
    }

    @Test
    public void shouldThrowEidasSamlParserResponseExceptionOnServerError() {
        EidasSamlParserProxy eidasSamlParserService = setUpEidasSamlParserService("/parse/server-error");

        assertThatThrownBy(() -> eidasSamlParserService.parse(eidasSamlParserRequest, "session_id"))
                .isInstanceOf(EidasSamlParserResponseException.class)
                .matches(e -> e.getCause().getMessage().startsWith(String.format(
                        "Exception of type [REMOTE_SERVER_ERROR] whilst contacting uri: %s/parse/server-error",
                        clientRule.baseUri().toString())));
    }

    @Test
    public void shouldThrowEidasSamlParserResponseExceptionOnClientError() {
        EidasSamlParserProxy eidasSamlParserService = setUpEidasSamlParserService("/parse/client-error");

        assertThatThrownBy(() -> eidasSamlParserService.parse(eidasSamlParserRequest, "session_id"))
                .isInstanceOf(EidasSamlParserResponseException.class)
                .matches(e -> e.getCause().getMessage().startsWith(String.format(
                        "Exception of type [CLIENT_ERROR] whilst contacting uri: %s/parse/client-error",
                        clientRule.baseUri().toString())));
    }

    @Test
    public void shouldSendJourneyIdInHeaders() {
        MDC.put(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name(), JOURNEY_ID);
        EidasSamlParserProxy eidasSamlParserService = setUpEidasSamlParserService("/parse/test-journey-id-header");
        eidasSamlParserService.parse(eidasSamlParserRequest, "session_id");
        assertThat(TestESPResource.headers.getFirst(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name())).isEqualTo(JOURNEY_ID);
    }

    private EidasSamlParserProxy setUpEidasSamlParserService(String url) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProxyNodeJsonClient jsonClient = new ProxyNodeJsonClient(
                new ErrorHandlingClient(ClientBuilder.newClient()),
                new JsonResponseProcessor(objectMapper),
                new IstioHeaderStorage()
        );

        return new EidasSamlParserProxy(
                jsonClient,
                UriBuilder.fromUri(clientRule.baseUri()).path(url).build()
        );
    }
}
