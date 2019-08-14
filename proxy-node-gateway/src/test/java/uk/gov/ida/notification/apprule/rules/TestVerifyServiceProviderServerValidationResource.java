package uk.gov.ida.notification.apprule.rules;

import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestGenerationBody;
import uk.gov.ida.notification.shared.Urls;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(Urls.VerifyServiceProviderUrls.GENERATE_HUB_AUTHN_REQUEST_ENDPOINT)
@Produces(MediaType.APPLICATION_JSON)
public class TestVerifyServiceProviderServerValidationResource {
    @POST
    @Valid
    public Response post(@Valid AuthnRequestGenerationBody request) {
        return Response.status(400).build();
    }
}
