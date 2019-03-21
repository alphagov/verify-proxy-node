package uk.gov.ida.notification.apprule.rules;

import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.SamlFailureResponseGenerationRequest;
import uk.gov.ida.notification.shared.Urls;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(Urls.TranslatorUrls.TRANSLATOR_ROOT)
@Produces(MediaType.APPLICATION_JSON)
public class TestTranslatorClientErrorResource {

    public static final String SAML_ERROR_BLOB = "encoded-saml-error-message";

    @POST
    @Path(Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH)
    public Response post(HubResponseTranslatorRequest hubResponseTranslatorRequest) {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST
    @Path(Urls.TranslatorUrls.GENERATE_FAILURE_RESPONSE_PATH)
    public Response generateErrorResponse(SamlFailureResponseGenerationRequest samlFailureResponseGenerationRequest) {
        return Response.ok().entity(SAML_ERROR_BLOB).build();
    }
}
