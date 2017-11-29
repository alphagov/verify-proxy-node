package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/SAML2/SSO")
public class EidasAuthnRequestResource {
    private EidasProxyNodeConfiguration configuration;
    private final EidasAuthnRequestTranslator authnRequestTranslator;

    public EidasAuthnRequestResource(EidasProxyNodeConfiguration configuration, EidasAuthnRequestTranslator authnRequestTranslator) {
        this.configuration = configuration;
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
        String hubAuthnRequest = authnRequestTranslator.translate(decodedEidasAuthnRequest);
        return buildSamlFormView(hubAuthnRequest);
    }

    private SamlFormView buildSamlFormView(String hubAuthnRequest) {
        String encodedHubAuthnRequest = Base64.encodeAsString(hubAuthnRequest);
        String hubUrl = configuration.getHubUrl().toString();
        String submitText = "Post Verify Authn Request to Hub";
        String samlRequest = SamlMessageType.SAML_REQUEST;
        return new SamlFormView( hubUrl, samlRequest, encodedHubAuthnRequest, submitText  );
    }
}
