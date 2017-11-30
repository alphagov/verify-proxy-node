package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.SamlFormViewMapper;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;
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
    private EidasProxyNodeConfiguration configuration;
    private final EidasAuthnRequestTranslator authnRequestTranslator;
    private SamlFormViewMapper samlFormViewMapper;
    private SamlParser parser;

    public EidasAuthnRequestResource(EidasProxyNodeConfiguration configuration,
                                     EidasAuthnRequestTranslator authnRequestTranslator,
                                     SamlFormViewMapper samlFormViewMapper,
                                     SamlParser parser) {
        this.configuration = configuration;
        this.authnRequestTranslator = authnRequestTranslator;
        this.samlFormViewMapper = samlFormViewMapper;
        this.parser = parser;
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
        EidasAuthnRequest eidasAuthnRequest = getEidasAuthnRequest(encodedEidasAuthnRequest);
        AuthnRequest hubAuthnRequest = authnRequestTranslator.translate(eidasAuthnRequest);
        return buildSamlFormView(hubAuthnRequest);
    }

    private EidasAuthnRequest getEidasAuthnRequest(String encodedEidasAuthnRequest) {
        String decodedEidasAuthnRequest = Base64.decodeAsString(encodedEidasAuthnRequest);
        AuthnRequest authnRequest = parser.parseSamlString(decodedEidasAuthnRequest, AuthnRequest.class);
        return new EidasAuthnRequest(authnRequest);
    }

    private SamlFormView buildSamlFormView(AuthnRequest hubAuthnRequest) {
        String hubUrl = configuration.getHubUrl().toString();
        String submitText = "Post Verify Authn Request to Hub";
        String samlRequest = SamlMessageType.SAML_REQUEST;
        return samlFormViewMapper.map(hubUrl, samlRequest, hubAuthnRequest, submitText);
    }
}
