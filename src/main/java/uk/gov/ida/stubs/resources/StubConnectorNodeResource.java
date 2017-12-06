package uk.gov.ida.stubs.resources;

import org.glassfish.jersey.internal.util.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.views.SamlFormView;
import uk.gov.ida.stubs.EidasAuthnRequestFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.TransformerException;
import java.net.URISyntaxException;

@Path("/connector-node")
@Produces(MediaType.TEXT_HTML)
public class StubConnectorNodeResource {
    private EidasAuthnRequestFactory eidasAuthnRequestFactory = new EidasAuthnRequestFactory();
    private SamlMarshaller samlMarshaller = new SamlMarshaller();

    @POST
    @Path("/eidas-authn-response")
    public String eidasAuthnResponse(@FormParam(SamlMessageType.SAML_RESPONSE) String encodedEidasResponse) {
        return Base64.decodeAsString(encodedEidasResponse);
    }

    @GET
    @Path("/eidas-authn-request")
    public SamlFormView eidasAuthnRequest() throws Throwable {
        String proxyNodeAuthnUrl = "/SAML2/SSO/POST";
        String samlRequest = SamlMessageType.SAML_REQUEST;
        String encodedAuthnRequest = buildEncodedAuthnRequest();
        String submitText = "POST eIDAS AuthnRequest to Proxy Node";
        return new SamlFormView(proxyNodeAuthnUrl, samlRequest, encodedAuthnRequest, submitText);
    }

    private String buildEncodedAuthnRequest() throws Throwable {
        AuthnRequest authnRequest = eidasAuthnRequestFactory.createEidasAuthnRequest(
                "any issuer entity id",
                "any destination",
                new DateTime(DateTimeZone.UTC)
        );
        String authnRequestString = samlMarshaller.samlObjectToString(authnRequest);
        return Base64.encodeAsString(authnRequestString);
    }
}
