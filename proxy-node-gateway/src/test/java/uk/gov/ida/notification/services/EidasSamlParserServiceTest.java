package uk.gov.ida.notification.services;

import com.google.common.collect.Lists;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.ida.notification.dto.EidasSamlParserRequest;
import uk.gov.ida.notification.dto.EidasSamlParserResponse;
import uk.gov.ida.notification.exceptions.EidasSamlParserResponseException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EidasSamlParserServiceTest {
    @Path("/parse")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestESPResource {

        @POST
        @Path("/valid")
        public EidasSamlParserResponse testValidParse(EidasSamlParserRequest eidasSamlParserRequest) {
            return new EidasSamlParserResponse(
                "request_id",
                "issuer",
                "pub_enc_key",
                "destination");
        }

        @POST
        @Path("/invalid")
        public EidasSamlParserResponse testInvalidParse(EidasSamlParserRequest eidasSamlParserRequest) {
            return new EidasSamlParserResponse(
                null,
                "issuer",
                "pub_enc_key",
                "");
        }

        @POST
        @Path("/server-error")
        public Response testServerError(EidasSamlParserRequest eidasSamlParserRequest) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        @POST
        @Path("/client-error")
        public Response testClientError(EidasSamlParserRequest eidasSamlParserRequest) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        @POST
        @Path("/incorrect-entity")
        public EidasSamlParserRequest testIncorrectEntity(EidasSamlParserRequest eidasSamlParserRequest) {
            return eidasSamlParserRequest;
        }
    }

    private final EidasSamlParserRequest eidasSamlParserRequest = new EidasSamlParserRequest("authn_request");

    @ClassRule
    public static final DropwizardClientRule clientRule = new DropwizardClientRule(new TestESPResource());

    @Test
    public void shouldReturnEidasSamlParserResponse() throws Exception {
        EidasSamlParserService eidasSamlParserService = setUpEidasSamlParserService("/parse/valid");

        EidasSamlParserResponse response = eidasSamlParserService.parse(eidasSamlParserRequest);

        assertEquals("request_id", response.getRequestId());
        assertEquals("issuer", response.getIssuer());
        assertEquals("pub_enc_key", response.getConnectorPublicEncryptionKey());
        assertEquals("destination", response.getDestination());
    }

    @Test
    public void shouldThrowEidasSamlParserResponseExceptionIfInvalidResponse() throws Exception {
        Logger logger = Logger.getLogger(EidasSamlParserResponseException.class.getName());
        Handler mockHandler = mock(Handler.class);
        logger.addHandler(mockHandler);
        ArgumentCaptor<LogRecord> loggingEventCaptor = ArgumentCaptor.forClass(LogRecord.class);
        EidasSamlParserService eidasSamlParserService = setUpEidasSamlParserService("/parse/invalid");

        assertExceptionAndMessage(eidasSamlParserService, "Invalid EidasSamlParserResponse");

        verify(mockHandler, times(2)).publish(loggingEventCaptor.capture());
        List<String> allLogRecords = loggingEventCaptor
            .getAllValues()
            .stream()
            .map(m -> m.getMessage())
            .sorted()
            .collect(Collectors.toList());

        List<String> expectedLogOutput = Lists.newArrayList(
            "Invalid EidasSamlParserResponse: Property 'destination' may not be empty",
            "Invalid EidasSamlParserResponse: Property 'requestId' may not be empty"
        );

        assertThat(expectedLogOutput).isEqualTo(allLogRecords);
    }

    @Test
    public void shouldThrowEidasSamlParserResponseExceptionIfResponseIs500() throws Exception {
        EidasSamlParserService eidasSamlParserService = setUpEidasSamlParserService("/parse/server-error");
        assertExceptionAndMessage(eidasSamlParserService, "Received a '500' status code response: Internal Server Error");
    }

    @Test
    public void shouldThrowEidasSamlParserResponseExceptionIfResponseIs400() throws Exception {
        EidasSamlParserService eidasSamlParserService = setUpEidasSamlParserService("/parse/client-error");
        assertExceptionAndMessage(eidasSamlParserService, "Received a '400' status code response: Bad Request");
    }

    @Test
    public void shouldThrowEidasSamlParserResponseExceptionIfIncorrectEntityReturned() throws Exception {
        EidasSamlParserService eidasSamlParserService = setUpEidasSamlParserService("/parse/incorrect-entity");
        assertExceptionAndMessage(eidasSamlParserService, "Error reading entity from input stream.");
    }

    private EidasSamlParserService setUpEidasSamlParserService(String url) throws Exception {
        Client client = ClientBuilder.newClient();
        return new EidasSamlParserService(
            client,
            new URL(clientRule.baseUri() + url).toString()
        );
    }

    private void assertExceptionAndMessage(EidasSamlParserService eidasSamlParserService, String expectedMessage) {
        try {
            EidasSamlParserResponse response = eidasSamlParserService.parse(eidasSamlParserRequest);
            fail("Expected exception not thrown");
        } catch (EidasSamlParserResponseException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }
}
