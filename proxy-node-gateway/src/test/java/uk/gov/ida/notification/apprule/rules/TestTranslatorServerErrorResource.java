package uk.gov.ida.notification.apprule.rules;

import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.shared.Urls;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(Urls.TranslatorUrls.TRANSLATOR_ROOT)
@Produces(MediaType.APPLICATION_JSON)
public class TestTranslatorServerErrorResource {

    @POST
    @Path(Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH)
    public Response post(HubResponseTranslatorRequest hubResponseTranslatorRequest) {
        return Response.serverError().build();
    }
}

