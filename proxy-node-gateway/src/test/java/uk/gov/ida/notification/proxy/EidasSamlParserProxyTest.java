package uk.gov.ida.notification.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import static org.junit.Assert.assertEquals;

public class EidasSamlParserProxyTest {
    @Path("/parse")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestESPResource {

        @POST
        public EidasSamlParserResponse testValidParse(EidasSamlParserRequest eidasSamlParserRequest) {
            return new EidasSamlParserResponse(
                "request_id",
                "issuer",
                "pub_enc_key",
                "destination");
        }
    }

    private final EidasSamlParserRequest eidasSamlParserRequest = new EidasSamlParserRequest("authn_request");

    @ClassRule
    public static final DropwizardClientRule clientRule = new DropwizardClientRule(new TestESPResource());

    @Test
    public void shouldReturnEidasSamlParserResponse() throws Exception {
        EidasSamlParserProxy eidasSamlParserService = setUpEidasSamlParserService("/parse");

        EidasSamlParserResponse response = eidasSamlParserService.parse(eidasSamlParserRequest);

        assertEquals("request_id", response.getRequestId());
        assertEquals("issuer", response.getIssuer());
        assertEquals("pub_enc_key", response.getConnectorPublicEncryptionKey());
        assertEquals("destination", response.getDestination());
    }

    private EidasSamlParserProxy setUpEidasSamlParserService(String url) throws Exception {
        JsonClient jsonClient = new JsonClient(
            new ErrorHandlingClient(ClientBuilder.newClient()),
            new JsonResponseProcessor(new ObjectMapper())
        );
        return new EidasSamlParserProxy(
            jsonClient,
            UriBuilder.fromUri(clientRule.baseUri()).path(url).build()
        );
    }
}