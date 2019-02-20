package uk.gov.ida.notification.apprule.rules;

import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestGenerationBody;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;
import uk.gov.ida.notification.shared.Urls;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;

@Path(Urls.VerifyServiceProviderUrls.GENERATE_HUB_AUTHN_REQUEST_ENDPOINT)
@Produces(MediaType.APPLICATION_JSON)
public class TestVerifyServiceProviderResource {

    public static final String REQUEST_ID_HUB = "a hub request id";
    public static final String ENCODED_SAML_BLOB = Base64.encodeAsString("Encoded SAML blob!");

    @POST
    @Valid
    public AuthnRequestResponse post(@Valid AuthnRequestGenerationBody request) throws URISyntaxException {
        return new AuthnRequestResponse(ENCODED_SAML_BLOB, REQUEST_ID_HUB, new URI("http://www.hub.com"));
    }
}