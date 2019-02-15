package uk.gov.ida.notification.apprule.rules;

import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.shared.Urls;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Urls.EidasSamlParserUrls.EIDAS_AUTHN_REQUEST_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class TestEidasSamlResource {

    @POST
    @Valid
    public EidasSamlParserResponse post(@Valid EidasSamlParserRequest request) {
        return new EidasSamlParserResponse("eidas request id", "issuer", "cert", "destination");
    }
}