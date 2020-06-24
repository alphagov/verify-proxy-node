package uk.gov.ida.notification.stubconnector.metadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.common.shared.configuration.EncodedPrivateKeyConfiguration;
import uk.gov.ida.common.shared.configuration.X509CertificateConfiguration;
import uk.gov.ida.notification.stubconnector.StubConnectorConfiguration;
import uk.gov.ida.notification.stubconnector.ConnectorNodeCredentialConfiguration;
import uk.gov.ida.saml.core.test.TestCertificateStrings;

import java.net.URI;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataGeneratorTest {

    private static final String ACS_URL = "acs url";
    private static final String ENTITY_ID = "http://localhost/entityId";

    @Mock
    private StubConnectorConfiguration configuration;

    @Test
    public void shouldCreateSignedConnectorNodeMetadata() throws Exception {

        var templateConfig = new HashMap<String, String>();
        templateConfig.put("wantSignedAssertions", "true");
        templateConfig.put("assertionConsumerServiceUrl", ACS_URL);

        var certConfig = new X509CertificateConfiguration(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT);
        var privateKeyConfig = new EncodedPrivateKeyConfiguration(TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY);
        var credentialConfiguration = new ConnectorNodeCredentialConfiguration(
                certConfig,
                certConfig,
                certConfig,
                privateKeyConfig,
                privateKeyConfig,
                privateKeyConfig
        );

        when(configuration.getConnectorNodeEntityId()).thenReturn(URI.create(ENTITY_ID));
        when(configuration.getConnectorNodeMetadataExpiryMonths()).thenReturn(1);
        when(configuration.getCredentialConfiguration()).thenReturn(credentialConfiguration);
        when(configuration.getConnectorNodeTemplateConfig()).thenReturn(templateConfig);

        MetadataGenerator metadataGenerator = new MetadataGenerator(configuration);

        EntityDescriptor connectorMetadata = metadataGenerator.getConnectorMetadata();
        SPSSODescriptor spssoDescriptor = connectorMetadata.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        AssertionConsumerService assertionConsumerService = spssoDescriptor.getAssertionConsumerServices().get(0);
        assertThat(spssoDescriptor.getWantAssertionsSigned()).isTrue();
        assertThat(connectorMetadata.getEntityID()).isEqualTo(ENTITY_ID);
        assertThat(assertionConsumerService.getLocation()).isEqualTo(ACS_URL);
        assertThat(connectorMetadata.isSigned()).isTrue();
        assertThat(connectorMetadata.isValid()).isTrue();
    }
}