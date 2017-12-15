package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.EidasResponseGenerator;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.saml.SamlFormMessageType;
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
    private SamlFormViewBuilder samlFormViewBuilder;

    public HubResponseResource(EidasProxyNodeConfiguration configuration,
                               EidasResponseGenerator eidasResponseGenerator,
                               SamlFormViewBuilder samlFormViewBuilder) {
        this.connectorNodeUrl = configuration.getConnectorNodeUrl().toString();
        this.eidasResponseGenerator = eidasResponseGenerator;
        this.samlFormViewBuilder = samlFormViewBuilder;
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(@FormParam(SamlFormMessageType.SAML_RESPONSE) org.opensaml.saml.saml2.core.Response encodedHubResponse) throws Throwable {
        HubResponse hubResponse = HubResponse.fromResponse(encodedHubResponse);
        Response eidasResponse = eidasResponseGenerator.generate(hubResponse);
        return samlFormViewBuilder.buildResponse(connectorNodeUrl, eidasResponse, SUBMIT_TEXT);
    }
}
