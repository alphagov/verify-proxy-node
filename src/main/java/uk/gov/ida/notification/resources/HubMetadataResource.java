package uk.gov.ida.notification.resources;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/hub-metadata")
public class HubMetadataResource {
    private final String MEDIA_TYPE_SAMLMETADATA_XML = "application/samlmetadata+xml";

    @GET
    @Produces(MEDIA_TYPE_SAMLMETADATA_XML)
    public Response getHubMetadata() throws IOException {
        String hubMetadata = Resources.toString(Resources.getResource("hub_metadata.xml"), Charsets.UTF_8);
        return Response.ok().entity(hubMetadata).build();
    }
}
