package uk.gov.ida.notification.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.exceptions.EidasSamlParserResponseException;

import javax.validation.Valid;
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
import static uk.gov.ida.saml.core.test.TestCertificateStrings.UNCHAINED_PUBLIC_CERT;

public class EidasSamlParserProxyTest {
    @Path("/parse")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestESPResource {

        @POST
        @Valid
        @Path("/valid")
        public EidasSamlParserResponse testValidParse(EidasSamlParserRequest eidasSamlParserRequest) {
            return new EidasSamlParserResponse("request_id", "issuer", UNCHAINED_PUBLIC_CERT, "destination");
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
    }

    private final EidasSamlParserRequest eidasSamlParserRequest = new EidasSamlParserRequest("authn_request");

    @ClassRule
    public static final DropwizardClientRule clientRule = new DropwizardClientRule(new TestESPResource());

    @Test
    public void shouldReturnEidasSamlParserResponse() {
        EidasSamlParserProxy eidasSamlParserService = setUpEidasSamlParserService("/parse/valid");

        EidasSamlParserResponse response = eidasSamlParserService.parse(eidasSamlParserRequest, "session_id");

        assertEquals("request_id", response.getRequestId());
        assertEquals("issuer", response.getIssuer());
        assertEquals(UNCHAINED_PUBLIC_CERT, response.getConnectorEncryptionPublicCertificate());
        assertEquals("destination", response.getDestination());
    }

    @Test
    public void shouldThrowEidasSamlParserResponseExceptionOnServerError() {
        EidasSamlParserProxy eidasSamlParserService = setUpEidasSamlParserService("/parse/server-error");

        try {
            EidasSamlParserResponse response = eidasSamlParserService.parse(eidasSamlParserRequest, "session_id");
            fail("Expected exception not thrown");
        } catch (EidasSamlParserResponseException e) {
            assertThat(e.getCause().getMessage())
                .startsWith(
                    String.format(
                        "Exception of type [REMOTE_SERVER_ERROR] whilst contacting uri: %s/parse/server-error",
                        clientRule.baseUri().toString()
                    )
                );
        }
    }

    @Test
    public void shouldThrowEidasSamlParserResponseExceptionOnClientError() {
        EidasSamlParserProxy eidasSamlParserService = setUpEidasSamlParserService("/parse/client-error");

        try {
            EidasSamlParserResponse response = eidasSamlParserService.parse(eidasSamlParserRequest, "session_id");
            fail("Expected exception not thrown");
        } catch (EidasSamlParserResponseException e) {
            assertThat(e.getCause().getMessage())
                .startsWith(
                    String.format(
                        "Exception of type [CLIENT_ERROR] whilst contacting uri: %s/parse/client-error",
                        clientRule.baseUri().toString()
                    )
                );
        }
    }

    private EidasSamlParserProxy setUpEidasSamlParserService(String url) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonClient jsonClient = new JsonClient(
                new ErrorHandlingClient(ClientBuilder.newClient()),
                new JsonResponseProcessor(objectMapper)
        );

        return new EidasSamlParserProxy(
                jsonClient,
                UriBuilder.fromUri(clientRule.baseUri()).path(url).build()
        );
    }
}
