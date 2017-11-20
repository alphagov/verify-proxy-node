package uk.gov.ida.notification.resources;

import net.shibboleth.utilities.java.support.security.IdentifierGenerationStrategy;
import org.glassfish.jersey.internal.util.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.w3c.dom.Node;
import uk.gov.ida.notification.saml.AuthnRequestFactory;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.views.SamlFormView;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/stubs")
@Produces(MediaType.TEXT_HTML)
public class TestStubsResource {
    private static final String FIXED_ID = "_0ac6a8af9fed04143875c565d97aed6b";

    IdentifierGenerationStrategy fixedIdentifierStrategy = new IdentifierGenerationStrategy(){
        @Nonnull
        @Override
        public String generateIdentifier() {
            return FIXED_ID;
        }
        public String generateIdentifier(boolean xmlSafe) {
            return generateIdentifier();
        }
    };

    private AuthnRequestFactory authnRequestFactory = new AuthnRequestFactory(fixedIdentifierStrategy);
    private SamlMarshaller samlMarshaller = new SamlMarshaller();

    @GET
    @Path("/connector-node")
    public SamlFormView eidasAuthnRequest() {
        AuthnRequest authnRequest = authnRequestFactory.createEidasAuthnRequest(
                "https://connector-node.eu",
                "https://proxy-node.uk/SAML2/SSO/POST",
                new DateTime(DateTimeZone.UTC)
        );
        String authnRequestString = samlMarshaller.samlObjectToString(authnRequest);
        String encodedAuthnRequest = Base64.encodeAsString(authnRequestString);

        return new SamlFormView("/SAML2/SSO/POST", SamlMessageType.SAML_REQUEST, encodedAuthnRequest, "POST eIDAS AuthnRequest to Proxy Node");
    }

    @POST
    @Path("/hub")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String stubHubAuthnRequest(@FormParam("SAMLRequest") String encodedAuthnRequest) {
        String decodedAuthnRequest = Base64.decodeAsString(encodedAuthnRequest);
        XMLObject message;
        try {
            message = new SamlParser().parseSamlString(decodedAuthnRequest);
        } catch (Exception toDo_handleThisProperly) {
            throw new RuntimeException(toDo_handleThisProperly);
        }
        Node authnElement = message.getDOM();
        System.out.println("HELLO:" + authnElement.getAttributes().getNamedItem("ID").getTextContent());
        String id = authnElement.getAttributes().getNamedItem("ID").getTextContent();
        return "Welcome to HUB: " + id;
    }


}
