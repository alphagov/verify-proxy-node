package uk.gov.ida.notification.eidassaml.apprule;

import io.dropwizard.testing.ConfigOverride;
import keystore.KeyStoreResource;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.ClassRule;
import org.junit.Rule;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.opensaml.utils.ObjectUtils;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.apprule.rules.MetadataClientRule;
import uk.gov.ida.notification.eidassaml.RequestDto;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class EidasSamlParserAppRuleTestBase {

    @ClassRule
    public static final MetadataClientRule metadataClientRule;

    protected static final String CONNECTOR_NODE_ENTITY_ID = "http://connector-node:8080/ConnectorResponderMetadata";

    private final SamlObjectMarshaller marshaller = new SamlObjectMarshaller();

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
    public EidasSamlParserAppRule eidasSamlParserAppRule = new EidasSamlParserAppRule(
            ConfigOverride.config("proxyNodeAuthnRequestUrl", "http://proxy-node/SAML2/SSO/POST"),
            ConfigOverride.config("connectorMetadataConfiguration.url", metadataClientRule.baseUri() + "/connector-node/metadata"),
            ConfigOverride.config("connectorMetadataConfiguration.expectedEntityId", "http://connector-node:8080/ConnectorResponderMetadata"),
            ConfigOverride.config("connectorMetadataConfiguration.trustStore.type", "file"),
            ConfigOverride.config("connectorMetadataConfiguration.trustStore.store", truststore.getAbsolutePath()),
            ConfigOverride.config("connectorMetadataConfiguration.trustStore.password", truststore.getPassword())
    );

    protected Response postEidasAuthnRequest(AuthnRequest eidasAuthnRequest) throws MarshallingException {
        System.out.println(marshaller.transformToString(eidasAuthnRequest));

        RequestDto request = new RequestDto();
        request.authnRequest = Base64.encodeAsString(ObjectUtils.toString(eidasAuthnRequest));

        Response response = null;
        try {
            response = eidasSamlParserAppRule.target("/eidasAuthnRequest")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        } catch (URISyntaxException e) {
            fail(e);
        }

        return response;
    }
}
