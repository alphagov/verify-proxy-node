package uk.gov.ida.stubs.resources;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@Path("/stub-idp")
@Produces(MediaType.TEXT_HTML)
public class StubIdpResource {
    private final SamlObjectMarshaller marshaller = new SamlObjectMarshaller();

    @POST
    @Path("/request")
    public SamlFormView hubAuthnRequest(
            @FormParam(SamlFormMessageType.SAML_REQUEST) AuthnRequest hubAuthnRequest,
            @FormParam("RelayState") String relayState) throws Throwable {
        String proxyNodeHubResponseUrl = "/SAML2/Response/POST";
        String samlResponse = SamlFormMessageType.SAML_RESPONSE;
        String encodedHubResponse = buildEncodedHubResponse();
        String submitText = "POST Hub Response to Proxy Node";
        return new SamlFormView(proxyNodeHubResponseUrl, samlResponse, encodedHubResponse, submitText, relayState);
    }

    private String buildEncodedHubResponse() throws Throwable {
        return Base64.encodeAsString(marshaller.transformToString(getResponseFromFile()));
    }

    private Response getResponseFromFile() throws IOException, ParserConfigurationException {
        String staticHubResponseFileName = "verify_idp_response.xml";
        String samlResponse = Resources.toString(Resources.getResource(staticHubResponseFileName), Charsets.UTF_8);
        return new SamlParser().parseSamlString(samlResponse);
    }
}
