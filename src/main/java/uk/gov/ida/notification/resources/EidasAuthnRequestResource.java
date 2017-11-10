package uk.gov.ida.notification.resources;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import se.litsec.eidas.opensaml.ext.SPType;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/SAML2/SSO")
public class EidasAuthnRequestResource {
    public static final Logger LOG = Logger.getLogger(EidasAuthnRequestResource.class.getName());
    private final SamlParser samlParser;

    public EidasAuthnRequestResource(SamlParser samlParser) {
        this.samlParser = samlParser;
    }

    @GET
    @Path("/Redirect")
    public Response handleRedirectBinding(@QueryParam("SAMLRequest") String encodedAuthnRequest) {
        handleAuthnRequest(encodedAuthnRequest);
        return Response.ok().build();
    }

    @POST
    @Path("/POST")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response handlePostBinding(@FormParam("SAMLRequest") String encodedAuthnRequest) {
        handleAuthnRequest(encodedAuthnRequest);
        return Response.ok().build();
    }

    private void handleAuthnRequest(String encodedAuthnRequest) {
        String authnRequestXML = Base64.decodeAsString(encodedAuthnRequest);
        AuthnRequest authnRequest = (AuthnRequest) samlParser.parseSamlString(authnRequestXML);

        LOG.info("Issuer: " + authnRequest.getIssuer().getValue());
        LOG.info("Destination: " + authnRequest.getDestination());

        SPType spType = (SPType) authnRequest.getExtensions().getOrderedChildren().get(0);
        RequestedAttributes requestedAttributes = (RequestedAttributes) authnRequest.getExtensions().getOrderedChildren().get(1);

        LOG.info("[eIDAS] SPType: " + spType.getType());

        requestedAttributes.getRequestedAttributes()
                .stream()
                .forEach((attr) -> LOG.info("[eIDAS] Requested attribute: " + attr.getName()));
    }
}
