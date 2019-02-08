package uk.gov.ida.notification.services;

import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.notification.dto.EidasSamlParserRequest;
import uk.gov.ida.notification.dto.EidasSamlParserResponse;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class EidasSamlParserServiceTest {
    @Path("/parse")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestESPResource {

        @POST
        public EidasSamlParserResponse testParse(EidasSamlParserRequest eidasSamlParserRequest) {
            return new EidasSamlParserResponse(
                "request_id",
                "issuer",
                "pub_enc_key",
                "destination");
        }
    }

    @ClassRule
    public static final DropwizardClientRule clientRule = new DropwizardClientRule(new TestESPResource());

    @Test
    public void shouldReturnEidasSamlParserResponse() throws Exception {
        Client client = ClientBuilder.newClient();
        EidasSamlParserService eidasSamlParserService = new EidasSamlParserService(
            client,
            new URL(clientRule.baseUri() + "/parse").toString()
        );

        EidasSamlParserRequest request = new EidasSamlParserRequest("authn_request");
        EidasSamlParserResponse response = eidasSamlParserService.parse(request);

        assertEquals(response.getRequestId(), "request_id");
        assertEquals(response.getIssuer(), "issuer");
        assertEquals(response.getConnectorPublicEncryptionKey(), "pub_enc_key");
        assertEquals(response.getDestination(), "destination");

    }
}
