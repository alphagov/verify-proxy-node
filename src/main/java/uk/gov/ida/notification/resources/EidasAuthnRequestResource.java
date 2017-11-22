package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.net.URI;

@Path("/SAML2/SSO")
public class EidasAuthnRequestResource {
    private final URI connectorNodeUrl;
    private final URI idpUrl;
    private final String SUBMIT_TEXT = "Submit";

    public EidasAuthnRequestResource(EidasProxyNodeConfiguration configuration) {
        this.idpUrl = configuration.getIdpUrl();
        this.connectorNodeUrl = configuration.getConnectorNodeUrl();
    }

    @GET
    @Path("/Redirect")
    public View handleRedirectBinding(@QueryParam(SamlMessageType.SAML_REQUEST) String encodedAuthnRequest) {
        return handleAuthnRequest();
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View handlePostBinding(@FormParam(SamlMessageType.SAML_REQUEST) String encodedAuthnRequest) {
        return handleAuthnRequest();
    }

    @POST
    @Path("/idp-response")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View hubResponse(@FormParam(SamlMessageType.SAML_RESPONSE) String encodedHubResponse) {
        String eidasSamlResponse = "Encoded eIDAS Saml Response";
        return new SamlFormView(connectorNodeUrl, SamlMessageType.SAML_RESPONSE, eidasSamlResponse, SUBMIT_TEXT);
    }

    private View handleAuthnRequest() {
        String hubAuthnRequest = "Encoded Hub Authn Request";
        return new SamlFormView(idpUrl, SamlMessageType.SAML_REQUEST, hubAuthnRequest, SUBMIT_TEXT);
    }
}
