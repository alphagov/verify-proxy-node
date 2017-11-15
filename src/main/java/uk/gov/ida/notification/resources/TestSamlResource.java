package uk.gov.ida.notification.resources;

import org.glassfish.jersey.internal.util.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.AuthnRequestFactory;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/test-saml")
@Produces(MediaType.TEXT_HTML)
public class TestSamlResource {
    private AuthnRequestFactory authnRequestFactory = new AuthnRequestFactory();
    private SamlMarshaller samlMarshaller = new SamlMarshaller();

    @GET
    @Path("/eidas-authn-request")
    public SamlFormView eidasAuthnRequest() {
        AuthnRequest authnRequest = authnRequestFactory.createEidasAuthnRequest(
                "https://connector-node.eu",
                "https://proxy-node.uk/SAML2/SSO/POST",
                new DateTime(DateTimeZone.UTC)
        );
        String authnRequestString = samlMarshaller.samlObjectToString(authnRequest);
        String encodedAuthnRequest = Base64.encodeAsString(authnRequestString);

        return new SamlFormView("/SAML2/SSO/POST", "SAMLRequest", encodedAuthnRequest, "POST eIDAS AuthnRequest to Proxy Node");
    }
}
