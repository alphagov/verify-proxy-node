package uk.gov.ida.notification.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import keystore.KeyStoreResource;
import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.notification.apprule.rules.AbstractSamlAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.AppRule;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.stubconnector.StubConnectorApplication;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Map;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_PUBLIC_CERT;

public class StubConnectorAppRuleTestBase extends AbstractSamlAppRuleTestBase {

    protected static final String ENTITY_ORG_NAME = "stub country org name";
    protected static final String ENTITY_ORG_DISPLAY_NAME = "stub country org display name";
    protected static final String ENTITY_ORG_URL = "http://stub-connector/homepage";
    protected static final String METADATA_CERTS_PUBLISH_PATH = "/proxy-node-md-certs-publish-path";
    protected static final String METADATA_PUBLISH_PATH = "/stub-connector-md-publish-path";
    protected static final String ENTITY_ID = "http://stub-connector/Connector";
    protected static final String ACS_URL = "http://stub-connector/SAML2/Response/POST";

    private static final KeyStoreResource METADATA_TRUSTSTORE = createMetadataTruststore();

    private Map<String, NewCookie> cookies;

    protected String getEidasRequest(AppRule<StubConnectorConfiguration> stubConnectorAppRule) throws URISyntaxException {
        final Response response = stubConnectorAppRule.target("/RequestSubstantial").request().get();
        final String message = response.readEntity(String.class);
        cookies = response.getCookies();

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new RuntimeException("Received response with status " + response.getStatus() + " from Connector. Message:\n" + message);
        }

        return message;
    }

    protected String getConnectorMetadata(AppRule<StubConnectorConfiguration> stubConnectorAppRule) throws URISyntaxException {
        final Response response = stubConnectorAppRule.target("/ConnectorMetadata").request().get();
        return response.readEntity(String.class);
    }

    protected String postEidasResponse(AppRule<StubConnectorConfiguration> stubConnectorAppRule, String samlForm) throws URISyntaxException {
        final String encodedResponse = Base64.encodeAsString(samlForm);
        return postResponse(stubConnectorAppRule, encodedResponse);
    }

    protected String postMalformedEidasResponse(AppRule<StubConnectorConfiguration> stubConnectorAppRule, String samlForm) throws URISyntaxException {
        final String encodedResponse = "not-a-base64-encoded-xml-start-tag" + Base64.encodeAsString(samlForm);
        return postResponse(stubConnectorAppRule, encodedResponse);
    }

    protected static AppRule<StubConnectorConfiguration> createStubConnectorAppRule(DropwizardClientRule metadataClientRule) {
        final String proxyNodeMetadataUrl = metadataClientRule.baseUri() + "/proxy-node/Metadata";
        return new AppRule<>(
                StubConnectorApplication.class,
                ConfigOverride.config("connectorNodeBaseUrl", "http://stub-connector"),
                ConfigOverride.config("connectorNodeEntityId", ENTITY_ID),

                ConfigOverride.config("proxyNodeMetadataConfiguration.url", proxyNodeMetadataUrl),
                ConfigOverride.config("proxyNodeMetadataConfiguration.expectedEntityId", "http://proxy-node/Metadata"),
                ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.type", "file"),
                ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.store", METADATA_TRUSTSTORE.getAbsolutePath()),
                ConfigOverride.config("proxyNodeMetadataConfiguration.trustStore.password", METADATA_TRUSTSTORE.getPassword()),

                ConfigOverride.config("credentialConfiguration.metadataSigningPublicKey.cert", TEST_PUBLIC_CERT),
                ConfigOverride.config("credentialConfiguration.samlSigningPublicKey.cert", TEST_PUBLIC_CERT),
                ConfigOverride.config("credentialConfiguration.samlEncryptionPublicKey.cert", TEST_PUBLIC_CERT),
                ConfigOverride.config("credentialConfiguration.metadataSigningPrivateKey.key", TEST_PRIVATE_KEY),
                ConfigOverride.config("credentialConfiguration.samlSigningPrivateKey.key", TEST_PRIVATE_KEY),
                ConfigOverride.config("credentialConfiguration.samlEncryptionPrivateKey.key", TEST_PRIVATE_KEY),

                ConfigOverride.config("credentialConfiguration.metadataSigningPublicKey.type", "x509"),
                ConfigOverride.config("credentialConfiguration.samlSigningPublicKey.type", "x509"),
                ConfigOverride.config("credentialConfiguration.samlEncryptionPublicKey.type", "x509"),
                ConfigOverride.config("credentialConfiguration.metadataSigningPrivateKey.type", "encoded"),
                ConfigOverride.config("credentialConfiguration.samlSigningPrivateKey.type", "encoded"),
                ConfigOverride.config("credentialConfiguration.samlEncryptionPrivateKey.type", "encoded"),

                ConfigOverride.config("connectorNodeTemplateConfig.entityId", ENTITY_ID),
                ConfigOverride.config("connectorNodeTemplateConfig.assertionConsumerServiceUrl", ACS_URL),
                ConfigOverride.config("connectorNodeTemplateConfig.organizationName", ENTITY_ORG_NAME),
                ConfigOverride.config("connectorNodeTemplateConfig.organizationDisplayName", ENTITY_ORG_DISPLAY_NAME),
                ConfigOverride.config("connectorNodeTemplateConfig.organizationUrl", ENTITY_ORG_URL),
                ConfigOverride.config("connectorNodeTemplateConfig.wantSignedAssertions", "true")
        ) {
            @Override
            protected void before() {
                waitForMetadata(proxyNodeMetadataUrl);
                super.before();
            }
        };
    }

    private static Form createForm(String encodedResponse) {
        return new Form()
                .param(SamlFormMessageType.SAML_RESPONSE, encodedResponse)
                .param("RelayState", "relay");
    }

    private String postResponse(AppRule<StubConnectorConfiguration> stubConnectorAppRule, String encodedResponse) throws URISyntaxException {
        final Form postForm = createForm(encodedResponse);
        final Invocation.Builder request = stubConnectorAppRule.target("/SAML2/Response/POST").request();

        if (cookies != null) {
            request.cookie(cookies.get("stub-connector-session"));
        }

        return request.post(Entity.form(postForm)).readEntity(String.class);
    }
}
