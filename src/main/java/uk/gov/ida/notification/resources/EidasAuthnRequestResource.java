package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/SAML2/SSO")
public class EidasAuthnRequestResource {
    private final String hubUrl;
    private final EidasAuthnRequestTranslator authnRequestTranslator;

    public EidasAuthnRequestResource(EidasProxyNodeConfiguration configuration, EidasAuthnRequestTranslator authnRequestTranslator) {
        this.hubUrl = configuration.getHubUrl().toString();
        this.authnRequestTranslator = authnRequestTranslator;
    }

    @GET
    @Path("/Redirect")
    public View handleRedirectBinding(@QueryParam(SamlMessageType.SAML_REQUEST) String encodedEidasAuthnRequest) {
        return handleAuthnRequest(encodedEidasAuthnRequest);
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View handlePostBinding(@FormParam(SamlMessageType.SAML_REQUEST) String encodedEidasAuthnRequest) {
        return handleAuthnRequest(encodedEidasAuthnRequest);
    }

    private View handleAuthnRequest(String encodedEidasAuthnRequest) {
        String decodedEidasAuthnRequest = Base64.decodeAsString(encodedEidasAuthnRequest);
        String encodedHubAuthnRequest = Base64.encodeAsString(authnRequestTranslator.translate(decodedEidasAuthnRequest));

        return new SamlFormView(
                hubUrl, SamlMessageType.SAML_REQUEST, encodedHubAuthnRequest, "Post Verify Authn Request to Hub"
        );
    }
}
