package uk.gov.ida.notification.eidassaml.apprule.base;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import keystore.KeyStoreResource;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.apprule.rules.AbstractSamlAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.TestMetadataResource;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.eidassaml.apprule.rules.EidasSamlParserAppRule;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlObjectSigner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Base64;

public class EidasSamlParserAppRuleTestBase extends AbstractSamlAppRuleTestBase {

    private static final KeyStoreResource METADATA_TRUSTSTORE = createMetadataTruststore();
    protected static final SamlObjectSigner SAML_OBJECT_SIGNER = createSamlObjectSigner();
    private static final SamlObjectMarshaller SAML_OBJECT_MARSHALLER = new SamlObjectMarshaller();

    protected static Response postEidasAuthnRequest(EidasSamlParserAppRule eidasSamlParserAppRule, AuthnRequest authnRequest) throws URISyntaxException {
        return eidasSamlParserAppRule
                .target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(createEspRequest(authnRequest), MediaType.APPLICATION_JSON_TYPE));
    }

    protected Response postBlankEidasAuthnRequest(EidasSamlParserAppRule eidasSamlParserAppRule) throws URISyntaxException {
        final String eidasAuthnRequest = Base64.getEncoder().encodeToString("".getBytes());
        final EidasSamlParserRequest request = new EidasSamlParserRequest(eidasAuthnRequest);

        return eidasSamlParserAppRule.target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
    }

    protected static EidasSamlParserRequest createEspRequest(AuthnRequest authnRequest) {
        return new EidasSamlParserRequest(createSamlBase64EncodedRequest(authnRequest));
    }

    protected static EidasSamlParserAppRule createEidasSamlParserRule(DropwizardClientRule metadataClientRule) {
        final String connectorMetadataUrl = metadataClientRule.baseUri() + "/connector-node/Metadata";
        return new EidasSamlParserAppRule(
                ConfigOverride.config("proxyNodeAuthnRequestUrl", "http://proxy-node/eidasAuthnRequest"),
                ConfigOverride.config("connectorMetadataConfiguration.url", connectorMetadataUrl),
                ConfigOverride.config("connectorMetadataConfiguration.expectedEntityId", TestMetadataResource.CONNECTOR_ENTITY_ID),
                ConfigOverride.config("connectorMetadataConfiguration.trustStore.type", "file"),
                ConfigOverride.config("connectorMetadataConfiguration.trustStore.store", METADATA_TRUSTSTORE.getAbsolutePath()),
                ConfigOverride.config("connectorMetadataConfiguration.trustStore.password", METADATA_TRUSTSTORE.getPassword())
        ) {
            @Override
            protected void before() {
                waitForMetadata(connectorMetadataUrl);
                super.before();
            }
        };
    }

    private static String createSamlBase64EncodedRequest(AuthnRequest authnRequest) {
        return Base64.getEncoder().encodeToString(SAML_OBJECT_MARSHALLER.transformToString(authnRequest).getBytes());
    }
}
