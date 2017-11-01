package uk.gov.ida.notification.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/verify-uk")
public class VerifyResource {
    @POST
    public String handlePost() {
        return "Success!";
    }
}
