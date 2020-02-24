package uk.gov.ida.eidas.metatron.resources;

import uk.gov.ida.eidas.metatron.domain.MetadataResolverService;
import uk.gov.ida.notification.contracts.CountryMetadataResponse;
import uk.gov.ida.notification.shared.logging.IngressEgressLogging;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URI;

@Path("/")
@IngressEgressLogging
public class MetatronResource {

    private final MetadataResolverService metadataResolverService;

    public MetatronResource(MetadataResolverService metadataResolverService) {
        this.metadataResolverService = metadataResolverService;
    }

    @GET
    @Path("/metadata/{entityId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Valid
    public CountryMetadataResponse getCountryMetadataResponse(@PathParam("entityId") URI entityId) {
        return this.metadataResolverService.getCountryMetadataResponse(entityId);
    }
}
