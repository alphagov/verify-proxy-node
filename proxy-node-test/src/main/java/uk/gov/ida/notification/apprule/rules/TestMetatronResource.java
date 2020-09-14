package uk.gov.ida.notification.apprule.rules;


import uk.gov.ida.notification.contracts.metadata.AssertionConsumerService;
import uk.gov.ida.notification.contracts.metadata.CountryMetadataResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static uk.gov.ida.notification.apprule.rules.TestMetadataResource.CONNECTOR_ENTITY_DESTINATION;
import static uk.gov.ida.notification.apprule.rules.TestMetadataResource.CONNECTOR_ENTITY_ID;
import static uk.gov.ida.notification.apprule.rules.TestMetadataResource.CONNECTOR_ENTITY_ID_2;
import static uk.gov.ida.notification.apprule.rules.TestMetadataResource.CONNECTOR_ENTITY_ID_2_DESTINATION;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_SECONDARY_CERT;

@Path("/")
public class TestMetatronResource {

    private final Map<String, String> map = Map.of(
            CONNECTOR_ENTITY_ID, CONNECTOR_ENTITY_DESTINATION,
            CONNECTOR_ENTITY_ID_2, CONNECTOR_ENTITY_ID_2_DESTINATION
    );

    @GET
    @Path("metadata/{entity-id}")
    public CountryMetadataResponse get(@PathParam("entity-id") String entityId) {

        String destination = map.getOrDefault(entityId, CONNECTOR_ENTITY_DESTINATION);

        return new CountryMetadataResponse(
                STUB_COUNTRY_PUBLIC_SECONDARY_CERT,
                STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                Collections.singletonList(new AssertionConsumerService(URI.create(destination), 0, true)),
                entityId,
                "CC");
    }
}
