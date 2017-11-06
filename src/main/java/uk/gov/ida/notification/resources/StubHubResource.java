package uk.gov.ida.notification.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/stub-hub")
public class StubHubResource {
    @POST
    public String handlePost(String requestBody) {
        return requestBody + " - Verified By Hub";
    }
}
