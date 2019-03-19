package uk.gov.ida.notification.apprule.rules;

import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.SamlFailureResponseGenerationRequest;
import uk.gov.ida.notification.shared.Urls;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.ida.notification.apprule.rules.TestTranslatorClientErrorResource.SAML_ERROR_BLOB;

@Path(Urls.TranslatorUrls.TRANSLATOR_ROOT)
@Produces(MediaType.APPLICATION_JSON)
public class TestTranslatorResource {

    public static final String SAML_SUCCESS_BLOB = Base64.encodeAsString("encoded-saml-success-message");

    private final List<HubResponseTranslatorRequest> translatorArgs;

    public TestTranslatorResource() {
        this.translatorArgs = new ArrayList<>();
    }

    @POST
    @Path(Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH)
    public String hubResponse(HubResponseTranslatorRequest hubResponseTranslatorRequest) {
        translatorArgs.clear();
        translatorArgs.add(hubResponseTranslatorRequest);
        return SAML_SUCCESS_BLOB;
    }

    @POST
    @Path(Urls.TranslatorUrls.GENERATE_FAILURE_RESPONSE_PATH)
    public Response generateErrorResponse(SamlFailureResponseGenerationRequest samlFailureResponseGenerationRequest) {
        return Response.ok().entity(SAML_ERROR_BLOB).build();
    }

    public List<HubResponseTranslatorRequest> getTranslatorArgs() {
        return Collections.unmodifiableList(translatorArgs);
    }
}
