package uk.gov.ida.notification.stubconnector;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.KeyStoreResource;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.apprule.rules.MetadataClientRule;
import uk.gov.ida.saml.core.test.TestEntityIds;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class MetricsAppRuleTests {

    @ClassRule
    public static final MetadataClientRule metadataClientRule;

    private static final KeyStoreResource truststore = aKeyStoreResource()
            .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
            .build();

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
            metadataClientRule = new MetadataClientRule();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        truststore.create();
    }

    @Rule
    public DropwizardAppRule<StubConnectorConfiguration> stubConnectorAppRule = new DropwizardAppRule<>(StubConnectorApplication.class, resourceFilePath("config.yml"),
            ConfigOverride.config("server.applicationConnectors[0].port", "0"),
            ConfigOverride.config("server.adminConnectors[0].port", "0"),
            ConfigOverride.config("connectorNodeBaseUrl", "http://localhost:0"),
            ConfigOverride.config("encryptionKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("encryptionKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("encryptionKeyPair.privateKey.key", TEST_PRIVATE_KEY),
            ConfigOverride.config("proxyNodeMetadataConfiguration.url", metadataClientRule.baseUri() + "/hub/metadata"),
            ConfigOverride.config("proxyNodeMetadataConfiguration.expectedEntityId", TestEntityIds.HUB_ENTITY_ID),
            ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.type", "file"),
            ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.store", truststore.getAbsolutePath()),
            ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.password", truststore.getPassword()),
            ConfigOverride.config("signingKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("signingKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("signingKeyPair.privateKey.key", TEST_PRIVATE_KEY)
    );

    private final Client client = new JerseyClientBuilder().build();

    @Test
    public void shouldLogPrometheusMetrics() {
        // make a request so we can count it
        Response response = client.target(UriBuilder.fromUri("http://localhost:"+stubConnectorAppRule.getLocalPort()+"/Metadata"))
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);

        // get the metrics
        response = client.target(UriBuilder.fromUri("http://localhost:"+stubConnectorAppRule.getAdminPort()+"/prometheus/metrics"))
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("uk_gov_ida_notification_stubconnector_resources_MetadataResource_connectorMetadata_count 1.0");
    }
}
