package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.saml.ResponseAssertionDecrypter;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.translation.HubResponse;
import uk.gov.ida.notification.saml.translation.HubResponseTranslator;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Path("/SAML2/Response")
public class HubResponseResource {
    private static final Logger LOG = Logger.getLogger(HubResponseResource.class.getName());

    private final HubResponseTranslator hubResponseTranslator;
    private final SamlFormViewBuilder samlFormViewBuilder;
    private final ResponseAssertionDecrypter assertionDecrypter;
    private final String connectorNodeUrl;

    public HubResponseResource(HubResponseTranslator hubResponseTranslator, SamlFormViewBuilder samlFormViewBuilder, ResponseAssertionDecrypter assertionDecrypter, String connectorNodeUrl) {
        this.assertionDecrypter = assertionDecrypter;
        this.connectorNodeUrl = connectorNodeUrl;
        this.hubResponseTranslator = hubResponseTranslator;
        this.samlFormViewBuilder = samlFormViewBuilder;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(@FormParam(SamlFormMessageType.SAML_RESPONSE) Response encryptedHubResponse) {
        Response decryptedHubResponse = assertionDecrypter.decrypt(encryptedHubResponse);
        HubResponse hubResponse = HubResponse.fromResponse(decryptedHubResponse);
        logHubResponse(hubResponse);
        Response eidasResponse = hubResponseTranslator.translate(hubResponse);
        logEidasResponse(eidasResponse);
        return samlFormViewBuilder.buildResponse(connectorNodeUrl, eidasResponse, "Post eIDAS Response SAML to Connector Node");
    }

    private void logHubResponse(HubResponse hubResponse) {
        LOG.info("[Hub Response] ID: " + hubResponse.getResponseId());
        LOG.info("[Hub Response] In response to: " + hubResponse.getInResponseTo());
        LOG.info("[Hub Response] Provided level of assurance: " + hubResponse.getProvidedLoa());
    }

    private void logEidasResponse(Response eidasResponse) {
        LOG.info("[eIDAS Response] ID: " + eidasResponse.getID());
        LOG.info("[eIDAS Response] In response to: " + eidasResponse.getInResponseTo());
    }
}
