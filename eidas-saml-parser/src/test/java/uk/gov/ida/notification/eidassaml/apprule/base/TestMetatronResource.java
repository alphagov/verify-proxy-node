package uk.gov.ida.notification.eidassaml.apprule.base;


import uk.gov.ida.notification.contracts.CountryMetadataResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.net.URI;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

@Path("/")
public class TestMetatronResource {

    @GET
    @Path("/{entity-id}")
    public CountryMetadataResponse get(@PathParam("entity-id") String entityId) {
        return new CountryMetadataResponse(
                TEST_RP_PUBLIC_SIGNING_CERT,
                TEST_RP_PUBLIC_ENCRYPTION_CERT,
                URI.create("https://foo.com"),
                entityId,
                "CC");
    }


}
