package uk.gov.ida.notification.translator.apprule.rules;

import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.TranslatedHubResponse;
import uk.gov.ida.notification.shared.Urls;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/vsp")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StubVspResource {

    @POST
    @Path(Urls.VerifyServiceProviderUrls.TRANSLATE_HUB_RESPONSE_ENDPOINT)
    public TranslatedHubResponse getTranslatedHubResponse(HubResponseTranslatorRequest hubResponseTranslatorRequest) {
        return new TranslatedHubResponse("", "", "", "");
    }
}
