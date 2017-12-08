package uk.gov.ida.stubs.resources;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.notification.CredentialRepository;
import uk.gov.ida.notification.ProxyNodeSigner;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.XmlObjectMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@Path("/stub-idp")
@Produces(MediaType.TEXT_HTML)
public class StubIdpResource {
    @POST
    @Path("/request")
    public SamlFormView hubAuthnRequest() throws Throwable {
        String proxyNodeHubResponseUrl = "/SAML2/Response/POST";
        String samlResponse = SamlMessageType.SAML_RESPONSE;
        String encodedHubResponse = buildEncodedHubResponse();
        String submitText = "POST HUB SAML to PROXY NODE";
        return new SamlFormView(proxyNodeHubResponseUrl, samlResponse, encodedHubResponse, submitText);
    }

    private String buildEncodedHubResponse() throws Throwable {
        Response response = getResponseFromFile();
        Response signedResponse = signResponseWithHubCredentials(response);
        String signedResponseAsText = new XmlObjectMarshaller().transformToString(signedResponse);
        return Base64.encodeAsString(signedResponseAsText);
    }

    private Response signResponseWithHubCredentials(Response response) throws Throwable {
        CredentialRepository credentialRepository = new CredentialRepository("local/hub_signing_primary.pk8", "local/hub_signing_primary.crt");
        Credential hubCredential = credentialRepository.getHubCredential();
        return new ProxyNodeSigner(new XmlObjectMarshaller()).sign(response, hubCredential);
    }

    private Response getResponseFromFile() throws IOException, ParserConfigurationException {
        String staticHubResponseFileName = "verify_idp_response.xml";
        String samlResponse = Resources.toString(Resources.getResource(staticHubResponseFileName), Charsets.UTF_8);
        return new SamlParser().parseSamlString(samlResponse);
    }
}
