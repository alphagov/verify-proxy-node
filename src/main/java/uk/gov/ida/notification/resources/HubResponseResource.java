package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.EidasResponseGenerator;
import uk.gov.ida.notification.HubResponseMapper;
import uk.gov.ida.notification.SamlFormViewMapper;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.translation.HubResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/SAML2/Response")
public class HubResponseResource {
    private final String connectorNodeUrl;
    private final String SUBMIT_TEXT = "Post eIDAS Response SAML to Connector Node";
    private EidasResponseGenerator eidasResponseGenerator;
    private SamlFormViewMapper viewMapper;
    private HubResponseMapper hubResponseMapper;

    public HubResponseResource(EidasProxyNodeConfiguration configuration,
                               EidasResponseGenerator eidasResponseGenerator,
                               SamlFormViewMapper viewMapper,
                               HubResponseMapper hubResponseMapper) {
        this.connectorNodeUrl = configuration.getConnectorNodeUrl().toString();
        this.eidasResponseGenerator = eidasResponseGenerator;
        this.viewMapper = viewMapper;
        this.hubResponseMapper = hubResponseMapper;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(@FormParam(SamlMessageType.SAML_RESPONSE) String encodedHubResponse) throws Throwable {
        HubResponse hubResponse = hubResponseMapper.map(encodedHubResponse);
        Response eidasResponse = eidasResponseGenerator.generate(hubResponse);
        return viewMapper.map(connectorNodeUrl, SamlMessageType.SAML_RESPONSE, eidasResponse, SUBMIT_TEXT);
    }
}
