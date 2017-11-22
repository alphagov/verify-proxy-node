package uk.gov.ida.stubs.resources;

import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.views.SamlFormView;
import uk.gov.ida.stubs.StubIdpConfiguration;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/stub-idp")
@Produces(MediaType.TEXT_HTML)
public class StubIdpResource {

    private StubIdpConfiguration configuration;

    public StubIdpResource(StubIdpConfiguration configuration) {
        this.configuration = configuration;
    }

    @POST
    @Path("/request")
    public SamlFormView hubAuthnRequest() throws URISyntaxException {
        URI proxyNodeIdpResponseUri = configuration.getProxyNodeIdpResponseUri();
        String samlResponse = SamlMessageType.SAML_RESPONSE;
        String encodedAuthnResponse = Base64.encodeAsString("hub idp response");
        String submitText = "POST IDP SAML to HUB";
        return new SamlFormView(proxyNodeIdpResponseUri, samlResponse, encodedAuthnResponse, submitText);
    }
}
