package uk.gov.ida.notification.stubconnector.support;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.KeyStoreResource;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.apprule.rules.MetadataClientRule;
import uk.gov.ida.notification.stubconnector.StubConnectorApplication;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;
import uk.gov.ida.saml.core.test.TestEntityIds;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static helpers.ResourceHelpers.resourceFilePath;
import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class StubConnectorAppRuleTestBase {

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

    private final Client client = new JerseyClientBuilder().build();

    @Rule
    public final DropwizardAppRule<StubConnectorConfiguration> stubConnectorAppRule = new DropwizardAppRule<>(StubConnectorApplication .class, resourceFilePath("config.yml"),
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

    public Response get(String path) {
        return client.target(UriBuilder.fromUri("http://localhost:"+stubConnectorAppRule.getLocalPort()+ path))
                .request()
                .get();
    }

    public Response getFromAdminPort(String path) {
        return client.target(UriBuilder.fromUri("http://localhost:"+stubConnectorAppRule.getAdminPort()+ path))
                .request()
                .get();
    }
}
