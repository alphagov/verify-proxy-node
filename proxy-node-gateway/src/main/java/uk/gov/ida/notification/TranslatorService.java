package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.exceptions.hubresponse.TranslatorResponseException;
import uk.gov.ida.notification.exceptions.saml.SamlParsingException;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

public class TranslatorService {
    private final Client translatorClient;
    private final String translatorUrl;
    private SamlParser samlParser;

    public TranslatorService(
            Client translatorClient,
            String translatorUrl,
            SamlParser samlParser) {
        this.translatorClient = translatorClient;
        this.translatorUrl = translatorUrl;
        this.samlParser = samlParser;
    }

    public Response getTranslatedResponse(Response encryptedHubResponse) {
        try {
            String samlMessage = new SamlObjectMarshaller().transformToString(encryptedHubResponse);
            Form form = new Form();
            form.param(SamlFormMessageType.SAML_RESPONSE, Base64.encodeAsString(samlMessage));

            String response = translatorClient.target(translatorUrl)
                    .request()
                    .post(Entity.form(form))
                    .readEntity(String.class);

            return samlParser.parseSamlString(response);
        } catch (SamlParsingException e) {
            throw new TranslatorResponseException(e);
        }
    }
}
