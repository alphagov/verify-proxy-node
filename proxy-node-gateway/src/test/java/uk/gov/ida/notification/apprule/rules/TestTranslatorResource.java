package uk.gov.ida.notification.apprule.rules;

import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.shared.Urls;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

@Path(Urls.TranslatorUrls.TRANSLATOR_ROOT)
@Produces(MediaType.APPLICATION_JSON)
public class TestTranslatorResource {

    private List<HubResponseTranslatorRequest> translatorArgList;

    public TestTranslatorResource() {
        this(new ArrayList<>());
    }

    public TestTranslatorResource(List<HubResponseTranslatorRequest> translatorArgList) {
        this.translatorArgList = translatorArgList;
    }

    public static final String SAML_BLOB = Base64.encodeAsString("I'm also going to be a SAML blob!");

    @POST
    @Path(Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH)
    public String hubResponse(HubResponseTranslatorRequest hubResponseTranslatorRequest) {
        translatorArgList.clear();
        translatorArgList.add(hubResponseTranslatorRequest);
        return SAML_BLOB;
    }
}