package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import keystore.KeyStoreResource;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opensaml.core.config.InitializationService;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.apprule.rules.MetadataClientRule;
import uk.gov.ida.notification.apprule.rules.StubConnectorAppRule;
import uk.gov.ida.notification.apprule.rules.ProxyNodeClientRule;
import uk.gov.ida.notification.saml.SamlFormMessageType;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Map;

import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.*;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class StubConnectorAppRuleTestBase {

    private Map<String, NewCookie> cookies;

    @ClassRule
    public static final MetadataClientRule metadataClientRule;

    @ClassRule
    public static final ProxyNodeClientRule proxyNodeClientRule;

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
            metadataClientRule = new MetadataClientRule();
            proxyNodeClientRule = new ProxyNodeClientRule();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        truststore.create();
    }

    @Rule
    public StubConnectorAppRule stubConnectorAppRule = new StubConnectorAppRule(
            ConfigOverride.config("proxyNodeEntityId", "http://connector-node:8080/ConnectorResponderMetadata"),

            ConfigOverride.config("connectorNodeBaseUrl", "http://stub-connector"),

            ConfigOverride.config("proxyNodeMetadataConfiguration.url", metadataClientRule.baseUri() + "/stub-connector/metadata"), //
            ConfigOverride.config("proxyNodeMetadataConfiguration.expectedEntityId", "http://connector-node:8080/ConnectorResponderMetadata"),
            ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.type", "file"),
            ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.store", truststore.getAbsolutePath()),
            ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.password", truststore.getPassword()),

            ConfigOverride.config("signingKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("signingKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("signingKeyPair.privateKey.key", TEST_PRIVATE_KEY),

            ConfigOverride.config("encryptionKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("encryptionKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("encryptionKeyPair.privateKey.key", TEST_PRIVATE_KEY)
    );

    protected String getEidasRequest() throws URISyntaxException {
        Response response = stubConnectorAppRule.target("/Request")
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

        if (cookies != null)
            request.cookie(cookies.get("JSESSIONID"));

        Response response = request.post(Entity.form(postForm));
        return response.readEntity(String.class);
    }
}
