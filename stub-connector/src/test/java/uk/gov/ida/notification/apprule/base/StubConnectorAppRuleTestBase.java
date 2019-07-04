package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import keystore.KeyStoreResource;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.apprule.rules.StubConnectorAppRule;
import uk.gov.ida.notification.apprule.rules.TestMetadataResource;
import uk.gov.ida.notification.apprule.rules.TestProxyNodeResource;
import uk.gov.ida.notification.saml.SamlFormMessageType;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Map;

import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class StubConnectorAppRuleTestBase {

    protected static final String METADATA_PUBLISH_PATH = "/stub-connector-md-publish-path";

    private Map<String, NewCookie> cookies;

    @ClassRule
    public static final DropwizardClientRule metadataClientRule;

    @ClassRule
    public static final DropwizardClientRule proxyNodeClientRule;

    private static final KeyStoreResource truststore = aKeyStoreResource()
            .withCertificate(
                    "VERIFY-FEDERATION",
                    aCertificate()
                            .withCertificate(METADATA_SIGNING_A_PUBLIC_CERT)
                            .build()
                            .getCertificate()
            )
            .build();

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
            metadataClientRule = new DropwizardClientRule(new TestMetadataResource());
            proxyNodeClientRule = new DropwizardClientRule(new TestProxyNodeResource());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        truststore.create();
    }

    @Rule
    public StubConnectorAppRule stubConnectorAppRule = new StubConnectorAppRule(
            ConfigOverride.config("connectorNodeBaseUrl", "http://stub-connector"),

            ConfigOverride.config("proxyNodeMetadataConfiguration.url", metadataClientRule.baseUri() + "/proxy-node/metadata"), //
            ConfigOverride.config("proxyNodeMetadataConfiguration.expectedEntityId", "http://proxy-node/Metadata"),
            ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.type", "file"),
            ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.store", truststore.getAbsolutePath()),
            ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.password", truststore.getPassword()),

            ConfigOverride.config("credentialConfiguration.type", "file"),
            ConfigOverride.config("credentialConfiguration.publicKey.type", "x509"),
            ConfigOverride.config("credentialConfiguration.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("credentialConfiguration.privateKey.key", TEST_PRIVATE_KEY),

            ConfigOverride.config("metadataPublishingConfiguration.metadataResourceFilePath", "metadata/test-stub-connector-metadata.xml"),
            ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", METADATA_PUBLISH_PATH)
    );

    protected String getEidasRequest() throws URISyntaxException {
        Response response = stubConnectorAppRule.target("/RequestSubstantial")
                .request()
                .get();

        cookies = response.getCookies();

        return response.readEntity(String.class);
    }

    protected String postEidasResponse(String samlForm) throws URISyntaxException {
        String encodedResponse = Base64.encodeAsString(samlForm);
        Form postForm = new Form()
                .param(SamlFormMessageType.SAML_RESPONSE, encodedResponse)
                .param("RelayState", "relay");

        Invocation.Builder request = stubConnectorAppRule.target("/SAML2/Response/POST")
                .request();

        if (cookies != null) {
            request.cookie(cookies.get("stub-connector-session"));
        }

        Response response = request.post(Entity.form(postForm));
        return response.readEntity(String.class);
    }
}
