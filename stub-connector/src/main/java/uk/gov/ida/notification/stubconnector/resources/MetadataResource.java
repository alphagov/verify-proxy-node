package uk.gov.ida.notification.stubconnector.resources;

import io.dropwizard.jersey.caching.CacheControl;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.XMLObjectSupport;
import uk.gov.ida.notification.stubconnector.metadata.MetadataGenerator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Path("/ConnectorMetadata")
@Produces(MediaType.APPLICATION_XML)
public class MetadataResource {

    private final MetadataGenerator metadataGenerator;

    public MetadataResource(MetadataGenerator metadataGenerator) {
        this.metadataGenerator = metadataGenerator;
    }

    @GET
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
    public Response getConnectorNodeMetadata() throws Exception {
        XMLObject xmlObject = metadataGenerator.getConnectorMetadata();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            XMLObjectSupport.marshallToOutputStream(xmlObject, outputStream);
            return Response.ok().entity(outputStream.toString(StandardCharsets.UTF_8)).build();
        }
    }
}
