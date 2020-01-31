package uk.gov.ida.eidas.metatron.apprule.rules;

import io.dropwizard.client.JerseyClientBuilder;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.eidas.metatron.resources.MetatronResource;
import uk.gov.ida.notification.contracts.CountryMetadataResponse;
import uk.gov.ida.saml.core.test.OpenSAMLRunner;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.SPSSODescriptorBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

@RunWith(OpenSAMLRunner.class)
public class MetatronTests {
    public static TestMetadataResource testMetadataResource;
    public static CreatesConfigDropwizardClientRule testMetadataServer;
    public static MetatronAppRule metatronAppRule;
    private static Client client;

    @ClassRule
    public static RuleChain ruleChain;

    static {
        try {
            ruleChain = init();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    public static RuleChain init() throws Exception {
        testMetadataResource = new TestMetadataResource();
        testMetadataServer =  new CreatesConfigDropwizardClientRule(testMetadataResource);
        waitWhile(() -> testMetadataServer.getPort() == 0, "Waiting for client rule port ");

        testMetadataResource.init(testMetadataServer.getPort());
        metatronAppRule = new MetatronAppRule();

        return RuleChain.outerRule(testMetadataServer).around(metatronAppRule);
    }

    private static void waitWhile(Supplier<Boolean> condition, String message) {
        LocalDateTime giveUpAfter = LocalDateTime.now().plusSeconds(15);
        while(condition.get()) {
            if ( LocalDateTime.now().isAfter(giveUpAfter)) {
                Assert.fail("Timed out while " + message);
            }
        }
    }

    @Test
    public void firstValidResolvesOk() {
        String entityId = getEntityId(TestMetadataResource.VALID_ONE);
        Response response = getClient().target(getUriString(entityId)).request().get();

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        CountryMetadataResponse actual = response.readEntity(CountryMetadataResponse.class);
        assertThat(actual.getCountryCode()).isEqualTo("VO");
        assertThat(actual.getDestination()).isEqualTo(URI.create("http://foo.com/bar"));
        assertThat(actual.getEntityId()).isEqualTo(entityId);
        assertThatCertsMatch(actual.getSamlEncryptionCertX509(), TEST_RP_PUBLIC_ENCRYPTION_CERT);
        assertThatCertsMatch(actual.getSamlSigningCertX509(), TEST_RP_PUBLIC_SIGNING_CERT);
    }

    @Test
    public void secondValidResolvesOk() {
        String entityId = getEntityId(TestMetadataResource.VALID_TWO);
        Response response = getClient().target(getUriString(entityId)).request().get();

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        CountryMetadataResponse actual = response.readEntity(CountryMetadataResponse.class);
        assertThat(actual.getCountryCode()).isEqualTo("VT");
        assertThat(actual.getDestination()).isEqualTo(URI.create("http://foo.com/bar"));
        assertThat(actual.getEntityId()).isEqualTo(entityId);
        assertThatCertsMatch(actual.getSamlEncryptionCertX509(), TEST_RP_PUBLIC_ENCRYPTION_CERT);
        assertThatCertsMatch(actual.getSamlSigningCertX509(), TEST_RP_PUBLIC_SIGNING_CERT);
    }

    @Test
    public void expiredReturns500() {
        String entityId = getEntityId(TestMetadataResource.EXPIRED);
        Response response = getClient().target(getUriString(entityId)).request().get();

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void untrustedReturns500() {
        String entityId = getEntityId(TestMetadataResource.UNTRUSTED);
        Response response = getClient().target(getUriString(entityId)).request().get();

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void unsignedReturns500() {
        String entityId = getEntityId(TestMetadataResource.UNSIGNED);
        Response response = getClient().target(getUriString(entityId)).request().get();

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void metadataMarkedDisabledInConfigIsBadRequest() {
        String entityId = getEntityId(TestMetadataResource.DISABLED);
        Response response = getClient().target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.BAD_REQUEST);
        assertResponseIsAnErrorMap(response);
    }

    @Test
    public void metadataThrowsError() {
        String entityId = getEntityId(TestMetadataResource.THROWS_ERROR);
        Response response = getClient().target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
        assertResponseIsAnErrorMap(response);
    }

    @Test
    public void entityNotRegistered() {
        String entityId = getEntityId(TestMetadataResource.DOES_NOT_EXIST);
        Response response = getClient().target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.BAD_REQUEST);
        assertResponseIsAnErrorMap(response);
    }

    private String getEntityId(String name) {
        return "http://localhost:" + testMetadataServer.getPort() + "/application/" + name + "/Metadata";
    }

    private String getUriString(String entityId) {
        return "http://" + UriBuilder.fromMethod(MetatronResource.class, "getCountryMetadataResponse").host("localhost").port(metatronAppRule.getLocalPort()).build(entityId).toString();
    }

    private Client getClient() {
        if ( client == null ) {
            client = new JerseyClientBuilder(metatronAppRule.getEnvironment())
                    .withProperty(ClientProperties.CONNECT_TIMEOUT, 10000)
                    .withProperty(ClientProperties.READ_TIMEOUT, 10000)
                    .build("test client");
        }
        return client;
    }

    private void assertThatCertsMatch(String samlEncryptionCertX509, String testRpPublicEncryptionCert) {
        assertThat(StringUtils.right(samlEncryptionCertX509, 10)).isEqualTo(StringUtils.right(testRpPublicEncryptionCert, 10));
    }

    private void assertResponseIsAnErrorMap(Response response) {
        Map map = response.readEntity(Map.class);
        assertThat(map.containsKey("code")).isTrue();
        assertThat(map.containsKey("message")).isTrue();
    }

    @Path("/")
    public static class TestMetadataResource {
        public static final String VALID_ONE = "valid-one";
        public static final String VALID_TWO = "valid-two";
        public static final String EXPIRED = "expired";
        public static final String UNSIGNED = "unsigned";
        public static final String UNTRUSTED = "untrusted";
        public static final String DISABLED = "disabled";
        public static final String THROWS_ERROR = "error";
        public static final String DOES_NOT_EXIST = "does-not-exist";


        private Map<String, String> connectorMetadatas;

        public static String buildEntityId(int port, String name) {
            return "http://localhost:" + port + "/application/" + name + "/Metadata";
        }

        public void init(int port) throws Exception {
            connectorMetadatas = new HashMap<>();

            MetadataFactory metadataFactory = new MetadataFactory();

            final String VALID_ONE_VALID_ENTITY_ID = buildEntityId(port, VALID_ONE);
            final String VALID_TWO_VALID_ENTITY_ID = buildEntityId(port, VALID_TWO);
            final String EXPIRED_ENTITY_ID = buildEntityId(port, EXPIRED);
            final String UNSIGNED_ENTITY_ID = buildEntityId(port, UNSIGNED);
            final String UNTRUSTED_ENTITY_ID = buildEntityId(port, UNTRUSTED);
            final String DISABLED_ENTITY_ID = buildEntityId(port, DISABLED);
            final String THROWS_ERROR_ENTITY_ID = buildEntityId(port, THROWS_ERROR);


            connectorMetadatas.put(
                    VALID_ONE,
                    metadataFactory.singleEntityMetadata(
                            buildConnectorEntityDescriptor(VALID_ONE_VALID_ENTITY_ID)));

            connectorMetadatas.put(
                    VALID_TWO,
                    metadataFactory.singleEntityMetadata(
                            buildConnectorEntityDescriptor(VALID_TWO_VALID_ENTITY_ID)));

            connectorMetadatas.put(
                    EXPIRED,
                    metadataFactory.singleEntityMetadata(
                            buildExpiredEntityDescriptor(EXPIRED_ENTITY_ID)));

            connectorMetadatas.put(
                    UNSIGNED,
                    metadataFactory.singleEntityMetadata(
                            buildUnsignedConnectorEntityDescriptor(UNSIGNED_ENTITY_ID)));

            connectorMetadatas.put(
                    UNTRUSTED,
                    metadataFactory.singleEntityMetadata(
                            buildConnectorEntityDescriptor(UNTRUSTED_ENTITY_ID)));

            connectorMetadatas.put(
                    DISABLED,
                    metadataFactory.singleEntityMetadata(
                            buildConnectorEntityDescriptor(DISABLED_ENTITY_ID)));

            connectorMetadatas.put(
                    THROWS_ERROR,
                    metadataFactory.singleEntityMetadata(
                            buildConnectorEntityDescriptor(THROWS_ERROR_ENTITY_ID)));


        }

        private EntityDescriptor buildConnectorEntityDescriptor(String entityId) throws Exception {

            return buildConnectorEntityDescriptor(entityId, METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY);
        }

        private EntityDescriptor buildConnectorEntityDescriptor(String entityId, String metadataSigningCert, String metadataSigningKey) throws Exception {

            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(entityId)
                    .withIdpSsoDescriptor(null)
                    .setAddDefaultSpServiceDescriptor(false)
                    .addSpServiceDescriptor(getSpssoDescriptor(TEST_RP_PUBLIC_ENCRYPTION_CERT, TEST_RP_PUBLIC_SIGNING_CERT))
                    .withValidUntil(DateTime.now().plusWeeks(2))
                    .withSignature(getSignature(metadataSigningCert, metadataSigningKey))
                    .build();
        }

        private EntityDescriptor buildExpiredEntityDescriptor(String entityId) throws Exception {
            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(entityId)
                    .withIdpSsoDescriptor(null)
                    .setAddDefaultSpServiceDescriptor(false)
                    .addSpServiceDescriptor(getSpssoDescriptor(TEST_RP_PUBLIC_ENCRYPTION_CERT, TEST_RP_PUBLIC_SIGNING_CERT))
                    .withValidUntil(DateTime.now().minusWeeks(2))
                    .withSignature(getSignature(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY))
                    .build();
        }

        private EntityDescriptor buildUnsignedConnectorEntityDescriptor(String entityId) throws Exception {

            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(entityId)
                    .withIdpSsoDescriptor(null)
                    .setAddDefaultSpServiceDescriptor(false)
                    .addSpServiceDescriptor(getSpssoDescriptor(TEST_RP_PUBLIC_ENCRYPTION_CERT, TEST_RP_PUBLIC_SIGNING_CERT))
                    .withValidUntil(DateTime.now().plusWeeks(2))
                    .withSignature(null)
                    .build();
        }

        private SPSSODescriptor getSpssoDescriptor(String encryptionCert, String signingCert) {
            KeyDescriptor encryptionKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
                    .withX509ForEncryption(encryptionCert)
                    .build();

            KeyDescriptor signingKeyDescriptor = KeyDescriptorBuilder.aKeyDescriptor()
                    .withX509ForSigning(signingCert)
                    .build();

            return SPSSODescriptorBuilder.anSpServiceDescriptor()
                    .withoutDefaultSigningKey()
                    .withoutDefaultEncryptionKey()
                    .addKeyDescriptor(signingKeyDescriptor)
                    .addKeyDescriptor(encryptionKeyDescriptor)
                    .build();
        }

        private Signature getSignature(String metadataSigningCert, String metadataSigningKey) {
            return SignatureBuilder.aSignature()
                    .withSigningCredential(new TestCredentialFactory(metadataSigningCert, metadataSigningKey).getSigningCredential())
                    .withX509Data(metadataSigningCert)
                    .build();
        }

        @GET
        @Path("/{country}/Metadata")
        public Response truststore(@PathParam("country") String country) {

            if (THROWS_ERROR.equals(country)) {
                return Response.serverError().build();
            }
            return Response.ok(connectorMetadatas.get(country)).build();
        }
    }

}
