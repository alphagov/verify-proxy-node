package uk.gov.ida.notification.resources;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import se.litsec.eidas.opensaml.ext.SPType;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.views.EidasAuthnRequestView;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.stream.Collectors;

@Path("/SAML2/SSO")
public class EidasAuthnRequestResource {
    private final SamlParser samlParser;

    public EidasAuthnRequestResource(SamlParser samlParser) {
        this.samlParser = samlParser;
    }

    @GET
    @Path("/Redirect")
    public EidasAuthnRequestView handleRedirectBinding(@QueryParam("SAMLRequest") String encodedAuthnRequest) {
        return handleAuthnRequest(encodedAuthnRequest);
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public EidasAuthnRequestView handlePostBinding(@FormParam("SAMLRequest") String encodedAuthnRequest) {
        return handleAuthnRequest(encodedAuthnRequest);
    }

    private EidasAuthnRequestView handleAuthnRequest(String encodedAuthnRequest) {
        String authnRequestXML = Base64.decodeAsString(encodedAuthnRequest);
        AuthnRequest authnRequest = (AuthnRequest) samlParser.parseSamlString(authnRequestXML);
        SPType spType = (SPType) authnRequest.getExtensions().getOrderedChildren().get(0);
        RequestedAttributes requestedAttributes = (RequestedAttributes) authnRequest.getExtensions().getOrderedChildren().get(1);

        return new EidasAuthnRequestView(
                authnRequest.getIssuer().getValue(),
                authnRequest.getDestination(),
                spType.getType().toString(),
                requestedAttributes.getRequestedAttributes().stream().map(Attribute::getName).collect(Collectors.toList()),
                authnRequest.getRequestedAuthnContext().getComparison().toString(),
                authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().stream().map(AuthnContextClassRef::getAuthnContextClassRef).collect(Collectors.toList())
        );
    }
}
