package uk.gov.ida.notification.apprule.rules;

import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.shared.Urls;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(Urls.EidasSamlParserUrls.EIDAS_AUTHN_REQUEST_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class TestEidasSamlServerErrorResource {
    @POST
    @Valid
    public Response post(@Valid EidasSamlParserRequest request) {
        return Response.serverError().build();
    }
}
