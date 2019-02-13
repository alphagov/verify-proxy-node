package uk.gov.ida.notification.proxy;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.X509CertificateDeserializer;
import uk.gov.ida.notification.contracts.X509CertificateSerializer;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.UNCHAINED_PUBLIC_CERT;

public class EidasSamlParserProxyTest {
    @Path("/parse")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestESPResource {

        @POST
        @Valid
        public EidasSamlParserResponse testValidParse(EidasSamlParserRequest eidasSamlParserRequest) {
            X509Certificate certificate = new X509CertificateFactory().createCertificate(UNCHAINED_PUBLIC_CERT);
            return new EidasSamlParserResponse("request_id", "issuer", certificate, "destination");
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
        assertEquals(new X509CertificateFactory().createCertificate(UNCHAINED_PUBLIC_CERT), response.getConnectorEncryptionPublicCertificate());
        assertEquals("destination", response.getDestination());
    }
    private EidasSamlParserProxy setUpEidasSamlParserService(String url) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule testModule = new SimpleModule("TestModule", Version.unknownVersion());
        testModule.addSerializer(new X509CertificateSerializer());
        testModule.addDeserializer(X509Certificate.class, new X509CertificateDeserializer());
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