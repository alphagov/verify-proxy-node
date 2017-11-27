package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/SAML2/SSO")
public class EidasAuthnRequestResource {

    private final String idpUrl;
    private final String SUBMIT_TEXT = "Submit";

    public EidasAuthnRequestResource(EidasProxyNodeConfiguration configuration) {
        this.idpUrl = configuration.getIdpUrl().toString();
    }

    @GET
    @Path("/Redirect")
    public View handleRedirectBinding(@QueryParam(SamlMessageType.SAML_REQUEST) String encodedAuthnRequest) {
        return handleAuthnRequest(encodedAuthnRequest);
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View handlePostBinding(@FormParam(SamlMessageType.SAML_REQUEST) String encodedAuthnRequest) {
        return handleAuthnRequest(encodedAuthnRequest);
    }

    private View handleAuthnRequest(String encodedAuthnRequest) {
        return new SamlFormView(idpUrl, SamlMessageType.SAML_REQUEST, encodedAuthnRequest,SUBMIT_TEXT);
    }
}
