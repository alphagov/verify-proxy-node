package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.opensaml.saml.saml2.core.AuthnRequest;
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
import java.util.logging.Logger;

@Path("/SAML2/SSO")
public class EidasAuthnRequestResource {
    private static final Logger LOG = Logger.getLogger(EidasAuthnRequestResource.class.getName());

    private EidasProxyNodeConfiguration configuration;
    private final HubAuthnRequestGenerator hubAuthnRequestGenerator;
    private SamlFormViewBuilder samlFormViewBuilder;

    public EidasAuthnRequestResource(EidasProxyNodeConfiguration configuration,
                                     HubAuthnRequestGenerator authnRequestTranslator,
                                     SamlFormViewBuilder samlFormViewBuilder) {
        this.configuration = configuration;
        this.hubAuthnRequestGenerator = authnRequestTranslator;
        this.samlFormViewBuilder = samlFormViewBuilder;
    }

    @GET
    @Path("/Redirect")
    public View handleRedirectBinding(@QueryParam(SamlFormMessageType.SAML_REQUEST) AuthnRequest encodedEidasAuthnRequest) {
        return handleAuthnRequest(encodedEidasAuthnRequest);
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public View handlePostBinding(@FormParam(SamlFormMessageType.SAML_REQUEST) AuthnRequest encodedEidasAuthnRequest) {
        return handleAuthnRequest(encodedEidasAuthnRequest);
    }

    private View handleAuthnRequest(AuthnRequest authnRequest) {
        EidasAuthnRequest eidasAuthnRequest = EidasAuthnRequest.buildFromAuthnRequest(authnRequest);
        logAuthnRequestInformation(eidasAuthnRequest);
        AuthnRequest hubAuthnRequest = hubAuthnRequestGenerator.generate(eidasAuthnRequest);
        return buildSamlFormView(hubAuthnRequest);
    }

    private SamlFormView buildSamlFormView(AuthnRequest hubAuthnRequest) {
        String hubUrl = configuration.getHubUrl().toString();
        String submitText = "Post Verify Authn Request to Hub";
        return samlFormViewBuilder.buildRequest(hubUrl, hubAuthnRequest, submitText);
    }


    private void logAuthnRequestInformation(EidasAuthnRequest eidasAuthnRequest) {
        LOG.info("[eIDAS AuthnRequest] Request ID: " + eidasAuthnRequest.getRequestId());
        LOG.info("[eIDAS AuthnRequest] Issuer: " + eidasAuthnRequest.getIssuer());
        LOG.info("[eIDAS AuthnRequest] Destination: " + eidasAuthnRequest.getDestination());
        LOG.info("[eIDAS AuthnRequest] SPType: " + eidasAuthnRequest.getSpType());
        LOG.info("[eIDAS AuthnRequest] Requested level of assurance: " + eidasAuthnRequest.getRequestedLoa());
        eidasAuthnRequest.getRequestedAttributes()
            .stream()
            .forEach((attr) -> LOG.info("[eIDAS AuthnRequest] Requested attribute: " + attr.getName()));
    }
}
