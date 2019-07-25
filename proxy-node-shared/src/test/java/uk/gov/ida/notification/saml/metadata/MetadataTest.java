package uk.gov.ida.notification.saml.metadata;

import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.UsageType;
import uk.gov.ida.notification.exceptions.metadata.ConnectorMetadataException;
import uk.gov.ida.notification.exceptions.metadata.InvalidMetadataException;
import uk.gov.ida.notification.exceptions.metadata.MissingMetadataException;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.helpers.TestMetadataBuilder;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadataTest {

    private static final String TEST_CONNECTOR_NODE_METADATA_FILE = "connector_node_metadata_template.xml";
    private static final String TEST_CONNECTOR_NODE_METADATA_ENTITY_ID = "http://connector-node:8080/ConnectorResponderMetadata";
    private static final String TEST_HUB_METADATA_FILE = "hub_metadata.xml";
    private static final String TEST_HUB_METADATA_ENTITY_ID = "http://stub-idp-one.local/SSO/POST";

    @Test
    public void shouldReturnConnectorNodeEncryptionPublicKeyFromMetadata() throws Exception {
        X509Certificate encryptionCert = new TestKeyPair().certificate;
        PublicKey expectedPublicKey = encryptionCert.getPublicKey();

        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_CONNECTOR_NODE_METADATA_FILE)
                .withEncryptionCert(encryptionCert)
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(metadataResolver).initialize();

        Metadata metadata = new Metadata(metadataCredentialResolver);
        PublicKey connectorNodeEncryptionPublicKey = metadata.getCredential(UsageType.ENCRYPTION, TEST_CONNECTOR_NODE_METADATA_ENTITY_ID, IDPSSODescriptor.DEFAULT_ELEMENT_NAME).getPublicKey();

        assertThat(expectedPublicKey).isEqualTo(connectorNodeEncryptionPublicKey);
    }

    @Test
    public void shouldReturnHubSigningPublicKeyFromMetadata() throws Exception {
        X509Certificate signingCert = new TestKeyPair().certificate;
        PublicKey expectedPublicKey = signingCert.getPublicKey();

        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_HUB_METADATA_FILE)
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(metadataResolver).initialize();

        Metadata metadata = new Metadata(metadataCredentialResolver);
        PublicKey hubSigningPublicKey = metadata.getCredential(UsageType.SIGNING, TEST_HUB_METADATA_ENTITY_ID, IDPSSODescriptor.DEFAULT_ELEMENT_NAME).getPublicKey();

        assertThat(expectedPublicKey).isEqualTo(hubSigningPublicKey);
    }

    @Test(expected = InvalidMetadataException.class)
    public void shouldErrorIfEncryptionPublicKeyElementIsEmpty() throws Exception {
        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_CONNECTOR_NODE_METADATA_FILE)
                .withEncryptionCert("")
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(metadataResolver).initialize();

        Metadata metadata = new Metadata(metadataCredentialResolver);
        metadata.getCredential(UsageType.ENCRYPTION, TEST_CONNECTOR_NODE_METADATA_ENTITY_ID, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test(expected = InvalidMetadataException.class)
    public void shouldErrorIfNoEncryptionPublicKeyElement() throws Exception {
        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_CONNECTOR_NODE_METADATA_FILE)
                .withNoEncryptionCert()
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(metadataResolver).initialize();

        Metadata metadata = new Metadata(metadataCredentialResolver);
        metadata.getCredential(UsageType.ENCRYPTION, TEST_CONNECTOR_NODE_METADATA_ENTITY_ID, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldErrorIfUnableToResolveMetadata() throws Exception {
        expectedEx.expect(MissingMetadataException.class);
        expectedEx.expectMessage("Unable to resolve metadata credentials");

        MetadataCredentialResolver metadataCredentialResolver = mock(MetadataCredentialResolver.class);
        when(metadataCredentialResolver.resolveSingle(any())).thenThrow(ResolverException.class);

        Metadata metadata = new Metadata(metadataCredentialResolver);
        metadata.getCredential(UsageType.ENCRYPTION, TEST_CONNECTOR_NODE_METADATA_ENTITY_ID, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldReturnConnectorDestinationURLFromMetadata() throws Exception {
        MetadataResolver metadataResolver = new TestMetadataBuilder("local-connector-metadata.xml").buildResolver("connector");
        String location = Metadata.getAssertionConsumerServiceLocation("http://localhost:55000/local-connector/metadata.xml", metadataResolver);

        assertThat("http://localhost:50300/SAML2/SSO/EidasResponse/POST").isEqualTo(location);
    }

    @Test(expected = ConnectorMetadataException.class)
    public void shouldThrowExceptionWhenConnectorDestinationURLEntityIdNotFound() throws Exception {
        MetadataResolver metadataResolver = new TestMetadataBuilder("local-connector-metadata.xml").buildResolver("connector");
        String location = Metadata.getAssertionConsumerServiceLocation("http://unknown-entity", metadataResolver);
    }
}
