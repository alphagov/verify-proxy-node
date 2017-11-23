package uk.gov.ida.stubs.resources;

import org.glassfish.jersey.internal.util.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.views.SamlFormView;
import uk.gov.ida.stubs.StubConnectorNodeConfiguration;
import uk.gov.ida.notification.saml.AuthnRequestFactory;
import uk.gov.ida.notification.saml.SamlMarshaller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/connector-node")
@Produces(MediaType.TEXT_HTML)
public class StubConnectorNodeResource {

    private AuthnRequestFactory authnRequestFactory = new AuthnRequestFactory();
    private SamlMarshaller samlMarshaller = new SamlMarshaller();
    private StubConnectorNodeConfiguration configuration;

    public StubConnectorNodeResource(StubConnectorNodeConfiguration configuration) {
        this.configuration = configuration;
    }

    @GET
    @Path("/eidas-authn-request")
    public SamlFormView eidasAuthnRequest() throws URISyntaxException {
        URI proxyNodeAuthnUri = configuration.getProxyNodeAuthnRequestUri();
        String samlRequest = SamlMessageType.SAML_REQUEST;
        String encodedAuthnRequest = buildEncodedAuthnRequest();
        String submitText = "POST eIDAS AuthnRequest to Proxy Node";
        return new SamlFormView(proxyNodeAuthnUri, samlRequest, encodedAuthnRequest, submitText);
    }

    private String buildEncodedAuthnRequest() {
        AuthnRequest authnRequest = authnRequestFactory.createEidasAuthnRequest(
                "any issuer entity id",
                "any destination",
                new DateTime(DateTimeZone.UTC)
        );
        String authnRequestString = samlMarshaller.samlObjectToString(authnRequest);
        return Base64.encodeAsString(authnRequestString);
    }
}
