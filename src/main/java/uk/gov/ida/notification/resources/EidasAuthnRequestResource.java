package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.EidasAuthnRequestMapper;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.HubAuthnRequestGenerator;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;
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
    private EidasProxyNodeConfiguration configuration;
    private final HubAuthnRequestGenerator hubAuthnRequestGenerator;
    private SamlFormViewBuilder samlFormViewBuilder;
    private EidasAuthnRequestMapper eidasAuthnRequestMapper;

    public EidasAuthnRequestResource(EidasProxyNodeConfiguration configuration,
                                     HubAuthnRequestGenerator authnRequestTranslator,
                                     SamlFormViewBuilder samlFormViewBuilder,
                                     EidasAuthnRequestMapper eidasAuthnRequestMapper) {
        this.configuration = configuration;
        this.hubAuthnRequestGenerator = authnRequestTranslator;
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.eidasAuthnRequestMapper = eidasAuthnRequestMapper;
    }

    @GET
    @Path("/Redirect")
    public View handleRedirectBinding(@QueryParam(SamlFormMessageType.SAML_REQUEST) String encodedEidasAuthnRequest) throws Throwable {
        return handleAuthnRequest(encodedEidasAuthnRequest);
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View handlePostBinding(@FormParam(SamlFormMessageType.SAML_REQUEST) String encodedEidasAuthnRequest) throws Throwable {
        return handleAuthnRequest(encodedEidasAuthnRequest);
    }

    private View handleAuthnRequest(String encodedEidasAuthnRequest) {
        EidasAuthnRequest eidasAuthnRequest = eidasAuthnRequestMapper.map(encodedEidasAuthnRequest);
        AuthnRequest hubAuthnRequest = hubAuthnRequestGenerator.generate(eidasAuthnRequest);
        return buildSamlFormView(hubAuthnRequest);
    }

    private SamlFormView buildSamlFormView(AuthnRequest hubAuthnRequest) {
        String hubUrl = configuration.getHubUrl().toString();
        String submitText = "Post Verify Authn Request to Hub";
        return samlFormViewBuilder.buildRequest(hubUrl, hubAuthnRequest, submitText);
    }
}
