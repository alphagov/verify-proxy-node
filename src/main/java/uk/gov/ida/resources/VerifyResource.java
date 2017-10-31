package uk.gov.ida.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/verify-uk")
@Produces(APPLICATION_JSON)
public class VerifyResource {
    @POST
    public String handlePost() {
        return "Success!";
    }
}
