package uk.gov.ida.eidas.metatron.apprule;

import io.dropwizard.testing.ConfigOverride;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.eidas.metatron.apprule.rules.CountryMetadataClientRule;
import uk.gov.ida.eidas.metatron.apprule.rules.MetatronAppRule;
import uk.gov.ida.eidas.metatron.apprule.rules.TestCountryMetadataResource;
import uk.gov.ida.eidas.metatron.resources.MetatronResource;
import uk.gov.ida.notification.contracts.CountryMetadataResponse;
import uk.gov.ida.saml.core.test.OpenSAMLRunner;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

@RunWith(OpenSAMLRunner.class)
public class MetatronAppRuleTests {

    @ClassRule
    public static CountryMetadataClientRule countryMetadataClientRule = new CountryMetadataClientRule(new TestCountryMetadataResource());

    @ClassRule
    public static MetatronAppRule metatronAppRule = new MetatronAppRule(
            ConfigOverride.config("countriesConfig", countryMetadataClientRule.getTempConfigFilePath())
    );

    @ClassRule
    public static final RuleChain orderedRules = RuleChain.outerRule(countryMetadataClientRule).around(metatronAppRule);

    @Test
    public void firstValidResolvesOk() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.VALID_ONE);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        CountryMetadataResponse actual = response.readEntity(CountryMetadataResponse.class);
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        assertThat(actual.getCountryCode()).isEqualTo("VO");
        assertThat(actual.getDestination()).isEqualTo(URI.create("http://foo.com/bar"));
        assertThat(actual.getEntityId()).isEqualTo(entityId);
        assertThatCertsMatch(actual.getSamlEncryptionCertX509(), TEST_RP_PUBLIC_ENCRYPTION_CERT);
        assertThatCertsMatch(actual.getSamlSigningCertX509(), TEST_RP_PUBLIC_SIGNING_CERT);
    }

    @Test
    public void secondValidResolvesOk() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.VALID_TWO);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        CountryMetadataResponse actual = response.readEntity(CountryMetadataResponse.class);
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        assertThat(actual.getCountryCode()).isEqualTo("VT");
        assertThat(actual.getDestination()).isEqualTo(URI.create("http://foo.com/bar"));
        assertThat(actual.getEntityId()).isEqualTo(entityId);
        assertThatCertsMatch(actual.getSamlEncryptionCertX509(), TEST_RP_PUBLIC_ENCRYPTION_CERT);
        assertThatCertsMatch(actual.getSamlSigningCertX509(), TEST_RP_PUBLIC_SIGNING_CERT);
    }

    @Test
    public void expiredReturns500() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.EXPIRED);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void untrustedReturns500() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.UNTRUSTED);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void unsignedReturns500() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.UNSIGNED);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void metadataMarkedDisabledInConfigIsBadRequestFromClient() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.DISABLED);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.BAD_REQUEST);
        assertResponseIsAnErrorMap(response);
    }

    @Test
    public void metadataEndpointThrows500Error() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.THROWS_ERROR);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
        assertResponseIsAnErrorMap(response);
    }

    @Test
    public void entityIdNotRegistered() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.DOES_NOT_EXIST);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.BAD_REQUEST);
        assertResponseIsAnErrorMap(response);
    }

    private String getEntityId(String name) {
        return "http://localhost:" + countryMetadataClientRule.getPort() + "/application/" + name + "/Metadata";
    }

    private String getUriString(String entityId) {
        return "http://" + UriBuilder.fromMethod(MetatronResource.class, "getCountryMetadataResponse").host("localhost").port(metatronAppRule.getLocalPort()).build(entityId).toString();
    }

    private void assertResponseIsAnErrorMap(Response response) {
        Map map = response.readEntity(Map.class);
        assertThat(map.containsKey("code")).isTrue();
        assertThat(map.containsKey("message")).isTrue();
    }

    private void assertThatCertsMatch(String actualCert, String expectedCert) {
        X509CertificateFactory factory = new X509CertificateFactory();
        X509Certificate actual = factory.createCertificate(actualCert);
        X509Certificate expected = factory.createCertificate(expectedCert);
        assertThat(actual).isEqualTo(expected);
    }
}
