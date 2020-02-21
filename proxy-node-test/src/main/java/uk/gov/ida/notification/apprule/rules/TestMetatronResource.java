package uk.gov.ida.notification.apprule.rules;


import uk.gov.ida.notification.contracts.metadata.AssertionConsumerService;
import uk.gov.ida.notification.contracts.metadata.CountryMetadataResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.net.URI;
import java.util.Collections;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_SECONDARY_CERT;

@Path("/")
public class TestMetatronResource {

    @GET
    @Path("metadata/{entity-id}")
    public CountryMetadataResponse get(@PathParam("entity-id") String entityId) {
        return new CountryMetadataResponse(
                STUB_COUNTRY_PUBLIC_SECONDARY_CERT,
                STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                Collections.singletonList(new AssertionConsumerService(URI.create("http://assertionConsumerService.net"), 0, true)),
                entityId,
                "CC");
    }
}
