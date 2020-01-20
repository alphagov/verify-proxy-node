package uk.gov.ida.notification.shared.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.contracts.CountryMetadataResponse;
import uk.gov.ida.notification.exceptions.proxy.MetatronResponseException;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MetatronProxyTest {

    @ClassRule
    public static final DropwizardClientRule testMetatronClientRule = new DropwizardClientRule(new TestMetatronResourceGoodResponse());

    @ClassRule
    public static final DropwizardClientRule testMetatronServerErrorClientRule = new DropwizardClientRule(new TestMetatronResourceServerErrorResponse());

    @ClassRule
    public static final DropwizardClientRule testMetatronClientErrorClientRule = new DropwizardClientRule(new TestMetatronResourceClientErrorResponse());

    private final String TEST_ENTITY_ID = "https://test-entity-id.com";

    private ProxyNodeJsonClient jsonClient = setupJsonClient();

    @Test
    public void shouldReturnMetatronResponse() {
        MetatronProxy metatronProxy = new MetatronProxy(
                jsonClient,
                UriBuilder
                        .fromUri(testMetatronClientRule.baseUri())
                        .path("/metadata")
                        .build()
        );

        CountryMetadataResponse countryMetadata = metatronProxy.getCountryMetadata(TEST_ENTITY_ID);

        assertThat(countryMetadata.getSamlSigningCertX509()).isEqualTo("SAMLSIGNINGCERTX509");
        assertThat(countryMetadata.getSamlEncryptionCertX509()).isEqualTo("SAMLENCRYPTIONCERTX509");
        assertThat(countryMetadata.getDestination()).isEqualTo(URI.create("https://destination.gov.uk"));
        assertThat(countryMetadata.getEntityId()).isEqualTo(TEST_ENTITY_ID);
        assertThat(countryMetadata.getCountryCode()).isEqualTo("CC");
    }

    @Test
    public void shouldThrowMetatronResponseExceptionIfServerIssue() {
        MetatronProxy metatronProxy = new MetatronProxy(
                jsonClient,
                UriBuilder
                        .fromUri(testMetatronServerErrorClientRule.baseUri())
                        .path("/metadata")
                        .build()
        );

        MetatronResponseException metatronResponseException = assertThrows(MetatronResponseException.class, () -> {
            metatronProxy.getCountryMetadata(TEST_ENTITY_ID);
        });

        assertThat(metatronResponseException.getResponseStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
        assertThat(metatronResponseException.getMessage()).isEqualTo(MetatronResponseException.ERROR_MESSAGE_FORMAT, TEST_ENTITY_ID);
    }

    @Test
    public void shouldThrowMetatronResponseExceptionIfClientIssue() {
        MetatronProxy metatronProxy = new MetatronProxy(
                jsonClient,
                UriBuilder
                        .fromUri(testMetatronClientErrorClientRule.baseUri())
                        .path("/metadata")
                        .build()
        );

        MetatronResponseException metatronResponseException = assertThrows(MetatronResponseException.class, () -> {
            metatronProxy.getCountryMetadata(TEST_ENTITY_ID);
        });

        assertThat(metatronResponseException.getResponseStatus()).isEqualTo(Response.Status.BAD_REQUEST);
        assertThat(metatronResponseException.getMessage()).isEqualTo(String.format(MetatronResponseException.ERROR_MESSAGE_FORMAT, TEST_ENTITY_ID));
    }

    private static ProxyNodeJsonClient setupJsonClient() {
        return new ProxyNodeJsonClient(
                new ErrorHandlingClient(ClientBuilder.newClient()),
                new JsonResponseProcessor(new ObjectMapper()),
                new IstioHeaderStorage()
        );
    }

    @Path("/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestMetatronResourceGoodResponse {

        @GET
        @Path("{entityId}")
        public CountryMetadataResponse testGetMetatronGoodResponse(@PathParam("entityId") String entityId) throws Exception {
            return new CountryMetadataResponse(
                    "SAMLSIGNINGCERTX509",
                    "SAMLENCRYPTIONCERTX509",
                    URI.create("https://destination.gov.uk"),
                    URLDecoder.decode(entityId, StandardCharsets.UTF_8.toString()),
                    "CC"
            );
        }
    }

    @Path("/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestMetatronResourceServerErrorResponse {

        @GET
        @Path("{entityId}")
        public Response testGetMetatronServerErrorResponse(@PathParam("entityId") String entityId) {
            return Response.serverError().build();
        }
    }

    @Path("/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestMetatronResourceClientErrorResponse {

        @GET
        @Path("{entityId}")
        public Response testGetMetatronClientErrorResponse(@PathParam("entityId") String entityId) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
