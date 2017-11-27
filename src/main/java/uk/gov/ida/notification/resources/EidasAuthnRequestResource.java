package uk.gov.ida.notification.resources;

import io.dropwizard.views.View;
import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/SAML2/SSO")
public class EidasAuthnRequestResource {
    private final String hubUrl;
    private final String proxyNodeEntityId;
    private final EidasAuthnRequestTranslator authnRequestTranslator;
    private final SamlParser samlParser;
    private final SamlMarshaller samlMarshaller;

    public EidasAuthnRequestResource(EidasProxyNodeConfiguration configuration, EidasAuthnRequestTranslator authnRequestTranslator, SamlParser samlParser, SamlMarshaller samlMarshaller) {
        this.hubUrl = configuration.getHubUrl().toString();
        this.proxyNodeEntityId = configuration.getProxyNodeEntityId();
        this.authnRequestTranslator = authnRequestTranslator;
        this.samlParser = samlParser;
        this.samlMarshaller = samlMarshaller;
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
        AuthnRequest eidasAuthnRequest = (AuthnRequest) samlParser.parseSamlString(decodedEidasAuthnRequest);
        AuthnRequest hubAuthnRequest = authnRequestTranslator.translate(eidasAuthnRequest, proxyNodeEntityId, hubUrl);
        String encodedHubAuthnRequest = Base64.encodeAsString(samlMarshaller.samlObjectToString(hubAuthnRequest));
        return new SamlFormView(hubUrl, SamlMessageType.SAML_REQUEST, encodedHubAuthnRequest, "Post Verify Authn Request to Hub");
    }
}
