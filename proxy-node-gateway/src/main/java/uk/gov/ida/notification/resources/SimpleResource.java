package uk.gov.ida.notification.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class SimpleResource {
    private final URI errorPageRedirectUrl;

    public SimpleResource(URI errorPageRedirectUrl) {
        this.errorPageRedirectUrl = errorPageRedirectUrl;
    }

    @GET
    @Path("/")
    public Response getRoot() {
        return Response.status(Response.Status.SEE_OTHER).location(this.errorPageRedirectUrl).build();
    }


}
