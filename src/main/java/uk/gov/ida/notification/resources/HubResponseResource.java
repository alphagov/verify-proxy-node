package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.HubResponseTranslator;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/SAML2/Response")
public class HubResponseResource {
    private final String connectorNodeUrl;
    private final String SUBMIT_TEXT = "Submit";
    private SamlParser samlParser;
    private HubResponseTranslator hubResponseTranslator;
    private SamlMarshaller samlMarshaller;

    public HubResponseResource(EidasProxyNodeConfiguration configuration, SamlParser samlParser, HubResponseTranslator hubResponseTranslator, SamlMarshaller samlMarshaller) {
        this.connectorNodeUrl = configuration.getConnectorNodeUrl().toString();
        this.samlParser = samlParser;
        this.hubResponseTranslator = hubResponseTranslator;
        this.samlMarshaller = samlMarshaller;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(@FormParam(SamlMessageType.SAML_RESPONSE) String encodedHubResponse) {
        String idpResponseSamlString = Base64.decodeAsString(encodedHubResponse);
        Response idpResponse = (Response) samlParser.parseSamlString(idpResponseSamlString);
        Response eidasResponse = hubResponseTranslator.translate(idpResponse);
        String eidasSamlString = samlMarshaller.samlObjectToString(eidasResponse);
        String encodedEidasResponse = Base64.encodeAsString(eidasSamlString);
        return new SamlFormView(connectorNodeUrl, SamlMessageType.SAML_RESPONSE, encodedEidasResponse, SUBMIT_TEXT);
    }
}
