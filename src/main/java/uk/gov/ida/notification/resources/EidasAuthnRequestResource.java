package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URI;

@Path("/SAML2/SSO")
public class EidasAuthnRequestResource {

    private URI hubUrl;

    public EidasAuthnRequestResource(URI hubUrl) {
        this.hubUrl = hubUrl;
    }

    @GET
    @Path("/Redirect")
    public View handleRedirectBinding(@QueryParam("SAMLRequest") String encodedAuthnRequest) {
        return handleAuthnRequest();
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View handlePostBinding(@FormParam("SAMLRequest") String encodedAuthnRequest) {
        return handleAuthnRequest();
    }

    private View handleAuthnRequest() {
        return new SamlFormView(hubUrl.toString(), SamlFormView.SAML_REQUEST, "","Submit");
    }
}
