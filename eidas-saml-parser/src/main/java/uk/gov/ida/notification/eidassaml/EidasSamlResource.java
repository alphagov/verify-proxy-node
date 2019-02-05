package uk.gov.ida.notification.eidassaml;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/eidasAuthnRequest")
@Produces(MediaType.APPLICATION_JSON)
public class EidasSamlResource {
    @POST
    public ResponseDto post(RequestDto request) {
        return new ResponseDto("request_id", "issuer");
    }
}
