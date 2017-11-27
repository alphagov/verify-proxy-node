package uk.gov.ida.stubs.resources;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.views.SamlFormView;
import uk.gov.ida.stubs.StubIdpConfiguration;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/stub-idp")
@Produces(MediaType.TEXT_HTML)
public class StubIdpResource {

    private StubIdpConfiguration configuration;

    public StubIdpResource(StubIdpConfiguration configuration) {
        this.configuration = configuration;
    }

    @POST
    @Path("/request")
    public SamlFormView hubAuthnRequest() throws IOException {
        String proxyNodeHubResponseUrl = configuration.getProxyNodeHubResponseUrl().toString();
        String samlResponse = SamlMessageType.SAML_RESPONSE;
        String encodedHubResponse = buildEncodedHubResponse();
        String submitText = "POST HUB SAML to PROXY NODE";
        return new SamlFormView(proxyNodeHubResponseUrl, samlResponse, encodedHubResponse, submitText);
    }

    private String buildEncodedHubResponse() throws IOException {
        String staticHubResponseFileName = "verify_idp_response.xml";
        String samlResponse = getResourceFileContent(staticHubResponseFileName);
        return Base64.encodeAsString(samlResponse);
    }

    private String getResourceFileContent(String fileName) throws IOException {
        return Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
    }

}
