package uk.gov.ida.notification;

import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.helpers.HubResponseBuilder;
import uk.gov.ida.notification.saml.EidasResponseBuilder;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class TranslatorServiceTest extends SamlInitializedTest {
    @Path("/translate")
    public static class TestTranslateResource {
        private final SamlObjectMarshaller marshaller = new SamlObjectMarshaller();

        @POST
        public String testTranslate(@FormParam(SamlFormMessageType.SAML_RESPONSE) String hubResponse) {
            return marshaller.transformToString(new EidasResponseBuilder().withId("test").build());
        }
    }

    @ClassRule
    public static final DropwizardClientRule clientRule = new DropwizardClientRule(new TestTranslateResource());

    @Test
    public void shouldReturnTranslatedResponse() throws Exception {
        Client client = ClientBuilder.newClient();
        TranslatorService translatorService = new TranslatorService(
                client,
                new URL(clientRule.baseUri() + "/translate").toString(),
                new SamlParser());

        Response hubResponse = new HubResponseBuilder().build();
        Response eidasResponse = translatorService.getTranslatedResponse(hubResponse);

        assertEquals(eidasResponse.getID(), "test");
    }
}