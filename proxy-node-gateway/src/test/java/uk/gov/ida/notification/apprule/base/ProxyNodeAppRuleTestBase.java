package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import keystore.KeyStoreResource;

import org.glassfish.jersey.internal.util.Base64;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;

import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.apprule.rules.EidasProxyNodeAppRule;
import uk.gov.ida.notification.apprule.rules.MetadataClientRule;
import uk.gov.ida.notification.apprule.rules.TranslatorClientRule;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;

import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

import java.net.URISyntaxException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

public class ProxyNodeAppRuleTestBase {

    protected static final String CONNECTOR_NODE_ENTITY_ID = "http://connector-node:8080/ConnectorResponderMetadata";

    @ClassRule
    public static final MetadataClientRule metadataClientRule;

    @ClassRule
    public static final TranslatorClientRule translatorClientRule;

    private static final KeyStoreResource truststore = aKeyStoreResource()
            .withCertificate("VERIFY-FEDERATION", aCertificate().withCertificate(METADATA_SIGNING_A_PUBLIC_CERT).build().getCertificate())
            .build();

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
            metadataClientRule = new MetadataClientRule();
            translatorClientRule = new TranslatorClientRule();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        truststore.create();
    }

    protected final SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
    protected final Credential countrySigningCredential = new TestCredentialFactory(TEST_RP_PUBLIC_SIGNING_CERT, TEST_RP_PRIVATE_SIGNING_KEY).getSigningCredential();
    protected final SamlObjectSigner samlObjectSigner = new SamlObjectSigner(countrySigningCredential.getPublicKey(), countrySigningCredential.getPrivateKey(), TEST_RP_PUBLIC_SIGNING_CERT);

    @Rule
    public EidasProxyNodeAppRule proxyNodeAppRule = new EidasProxyNodeAppRule(
            ConfigOverride.config("proxyNodeEntityId", "http://proxy-node.uk"),
            ConfigOverride.config("proxyNodeResponseUrl", "http://proxy-node/SAML2/SSO/Response"),
            ConfigOverride.config("proxyNodeMetadataForConnectorNodeUrl", "http://proxy-node.uk"),
            ConfigOverride.config("hubUrl", "http://hub"),
            ConfigOverride.config("connectorNodeUrl", "http://connector-node:8080"),
            ConfigOverride.config("connectorNodeIssuerId", "http://connector-node:8080/ConnectorMetadata"),
            ConfigOverride.config("translatorUrl", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("connectorMetadataConfiguration.url", metadataClientRule.baseUri() + "/connector-node/metadata"),
            ConfigOverride.config("connectorMetadataConfiguration.expectedEntityId", "http://connector-node:8080/ConnectorResponderMetadata"),
            ConfigOverride.config("connectorMetadataConfiguration.trustStore.type", "file"),
            ConfigOverride.config("connectorMetadataConfiguration.trustStore.store", truststore.getAbsolutePath()),
            ConfigOverride.config("connectorMetadataConfiguration.trustStore.password", truststore.getPassword()),
            ConfigOverride.config("hubMetadataConfiguration.url", metadataClientRule.baseUri() + "/hub/metadata"),
            ConfigOverride.config("hubMetadataConfiguration.expectedEntityId", TestEntityIds.HUB_ENTITY_ID),
            ConfigOverride.config("hubMetadataConfiguration.trustStore.type", "file"),
            ConfigOverride.config("hubMetadataConfiguration.trustStore.store", truststore.getAbsolutePath()),
            ConfigOverride.config("hubMetadataConfiguration.trustStore.password", truststore.getPassword()),
            ConfigOverride.config("connectorFacingSigningKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("connectorFacingSigningKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("connectorFacingSigningKeyPair.privateKey.key", TEST_PRIVATE_KEY),
            ConfigOverride.config("hubFacingSigningKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("hubFacingSigningKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("hubFacingSigningKeyPair.privateKey.key", TEST_PRIVATE_KEY),
            ConfigOverride.config("hubFacingEncryptionKeyPair.publicKey.type", "x509"),
            ConfigOverride.config("hubFacingEncryptionKeyPair.publicKey.cert", TEST_PUBLIC_CERT),
            ConfigOverride.config("hubFacingEncryptionKeyPair.privateKey.key", TEST_PRIVATE_KEY)
    );

    protected Response postEidasAuthnRequest(AuthnRequest eidasAuthnRequest) throws URISyntaxException {
        String encodedRequest = Base64.encodeAsString(marshaller.transformToString(eidasAuthnRequest));
        Form postForm = new Form().param(SamlFormMessageType.SAML_REQUEST, encodedRequest);

        return proxyNodeAppRule.target("/SAML2/SSO/POST")
            .request()
            .post(Entity.form(postForm));
    }

    protected Response postHubResponse(org.opensaml.saml.saml2.core.Response hubResponse) throws URISyntaxException {
        String encodedResponse = Base64.encodeAsString(marshaller.transformToString(hubResponse));
        Form postForm = new Form().param(SamlFormMessageType.SAML_RESPONSE, encodedResponse);

        return proxyNodeAppRule.target("/SAML2/SSO/Response/POST")
            .request()
            .post(Entity.form(postForm));
    }
}
