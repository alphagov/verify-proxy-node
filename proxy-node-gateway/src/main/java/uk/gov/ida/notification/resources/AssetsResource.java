package uk.gov.ida.notification.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class AssetsResource {

    @GET
    @Path("/favicon.ico")
    public Response getFavicon() {
        return Response.ok().build();
    }
}
