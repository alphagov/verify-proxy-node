package uk.gov.ida.notification.apprule.rules;


import uk.gov.ida.notification.contracts.CountryMetadataResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.net.URI;

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
                URI.create("http://assertionConsumerService.net"),
                entityId,
                "CC");
    }
}
