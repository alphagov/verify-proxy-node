package uk.gov.ida.notification.contracts;

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MetatronResponseTest {

    @Path("/")
    public static class MetatronStubResource {

        @GET
        @Path("/get-metatron-response")
        @Produces(MediaType.APPLICATION_JSON)
        public MetatronResponse getMetatronResponse(
                @QueryParam("validator") String validator,
                @QueryParam("annotated") boolean annotated
        ) {
            String samlSigningCert = "SAMLSIGNINGCERTB64";
            String samlEncryptionCert = "SAMLENCRYPTIONCERTB64";
            URI destination = URI.create("https://www.example.com");
            String entityId = "entityId";
            String countryCode = "CC";

            switch(validator) {
                case "none":
                    break;
                case "blank":
                    samlSigningCert = "";
                    break;
                case "null":
                    samlEncryptionCert = null;
            }

            if (annotated) {
                return new MetatronResponseWithAnnotations(
                        samlSigningCert,
                        samlEncryptionCert,
                        destination,
                        entityId,
                        countryCode
                );
            } else {
                return new MetatronResponseWithoutAnnotations(
                        samlSigningCert,
                        samlEncryptionCert,
                        destination,
                        entityId,
                        countryCode
                );
            }
        }

        @POST
        @Path("/get-metatron-response")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public MetatronResponse postMetatronResponse(@Valid MetatronResponseWithAnnotations metatronResponse) {
            return metatronResponse;
        }

    }

    @ClassRule
    public static final DropwizardClientRule metatronStubClientRule = new DropwizardClientRule(new MetatronStubResource());

    private final JsonClient jsonClient = new JsonClient(
            new ErrorHandlingClient(ClientBuilder.newClient()),
            new JsonResponseProcessor(new ObjectMapper())
    );

    private final UriBuilder metatronStubUriBuilder = UriBuilder.fromUri(metatronStubClientRule.baseUri())
            .path("/get-metatron-response");

    @Test
    public void canBeSerialized() {
        MetatronResponseWithoutAnnotations metatronResponseWithoutAnnotations = jsonClient.get(
                metatronStubUriBuilder
                        .queryParam("validator", "none")
                        .queryParam("annotated", false)
                        .build(),
                MetatronResponseWithoutAnnotations.class
        );

        assertThat(metatronResponseWithoutAnnotations.getCountryCode()).isEqualTo("CC");
    }

    @Test
    public void getWithAnnotations_valuesCanBeBlank() {
        MetatronResponseWithAnnotations metatronResponseWithAnnotations = jsonClient.get(
                metatronStubUriBuilder
                        .queryParam("validator", "blank")
                        .queryParam("annotated", true)
                        .build(),
                MetatronResponseWithAnnotations.class
        );

        assertThat(metatronResponseWithAnnotations.getSamlSigningCertX509()).isEqualTo("");

    }

    @Test
    public void getWithAnnotations_valuesCanBeNull() {
        MetatronResponseWithAnnotations metatronResponseWithAnnotations = jsonClient.get(
                metatronStubUriBuilder
                        .queryParam("validator", "null")
                        .queryParam("annotated", true)
                        .build(),
                MetatronResponseWithAnnotations.class
        );

        assertThat(metatronResponseWithAnnotations.getSamlEncryptionCertX509()).isNull();
    }

    @Test
    public void postWithAnnotations_valuesCanNotBeBlank() {
        MetatronResponseWithoutAnnotations metatronResponseWithoutAnnotations = new MetatronResponseWithoutAnnotations(
                "",
                "SAMLENCRYPTIONCERTB64",
                URI.create("https://www.example.com"),
                "entityId",
                "CC"
        );

        ApplicationException applicationException = assertThrows(ApplicationException.class, () -> {
            jsonClient.post(
                    metatronResponseWithoutAnnotations,
                    metatronStubUriBuilder.build(),
                    MetatronResponseWithAnnotations.class
            );
        });

        assertTrue(applicationException.getMessage().contains("samlSigningCertX509 may not be empty"));
    }

    @Test
    public void postWithAnnotations_valuesCanNotBeNull() {
        MetatronResponseWithoutAnnotations metatronResponseWithoutAnnotations = new MetatronResponseWithoutAnnotations(
                "SAMLSIGNINGCERTB64",
                "SAMLENCRYPTIONCERTB64",
                null,
                "entityId",
                "CC"
        );

        ApplicationException applicationException = assertThrows(ApplicationException.class, () -> {
            jsonClient.post(
                    metatronResponseWithoutAnnotations,
                    metatronStubUriBuilder.build(),
                    MetatronResponseWithAnnotations.class
            );
        });

        assertTrue(applicationException.getMessage().contains("destination may not be null"));
    }
}
