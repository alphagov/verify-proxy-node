package uk.gov.ida.notification.resources;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Logger;

@Path("/hub-metadata/{environment}")
public class HubMetadataResource {
    private static Logger LOG = Logger.getLogger(HubMetadataResource.class.getName());
    private final String MEDIA_TYPE_SAMLMETADATA_XML = "application/samlmetadata+xml";

    @GET
    @Produces(MEDIA_TYPE_SAMLMETADATA_XML)
    public Response getHubMetadata(@PathParam("environment") String environment) {
        try {
            String hubMetadata = Resources.toString(
                    Resources.getResource(environment + "/metadata_for_hub.xml"),
                    Charsets.UTF_8);
            return Response.ok().entity(hubMetadata).build();
        } catch (IOException | IllegalArgumentException e) {
            LOG.info("Hub metadata not found: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
