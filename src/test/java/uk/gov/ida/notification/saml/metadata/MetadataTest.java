package uk.gov.ida.notification.saml.metadata;

import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import uk.gov.ida.notification.exceptions.MissingMetadataException;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.helpers.TestMetadataBuilder;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        Metadata metadata = new Metadata(metadataCredentialResolver);
        PublicKey connectorNodeEncryptionPublicKey = metadata.getEncryptionPublicKey(TEST_CONNECTOR_NODE_METADATA_ENTITY_ID);

        assertEquals(expectedPublicKey, connectorNodeEncryptionPublicKey);
    }

    @Test
    public void shouldReturnHubSigningPublicKeyFromMetadata() throws Exception {
        X509Certificate signingCert = new TestKeyPair().certificate;
        PublicKey expectedPublicKey = signingCert.getPublicKey();

        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_HUB_METADATA_FILE)
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        Metadata metadata = new Metadata(metadataCredentialResolver);
        PublicKey hubSigningPublicKey = metadata.getSigningPublicKey(TEST_HUB_METADATA_ENTITY_ID);

        assertEquals(expectedPublicKey, hubSigningPublicKey);
    }

    @Test(expected = MissingMetadataException.class)
    public void shouldErrorIfEncryptionPublicKeyElementIsEmpty() throws Exception {
        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_CONNECTOR_NODE_METADATA_FILE)
                .withEncryptionCert("")
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        Metadata metadata = new Metadata(metadataCredentialResolver);
        metadata.getEncryptionPublicKey(TEST_CONNECTOR_NODE_METADATA_ENTITY_ID);
    }

    @Test(expected = MissingMetadataException.class)
    public void shouldErrorIfNoEncryptionPublicKeyElement() throws Exception {
        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_CONNECTOR_NODE_METADATA_FILE)
                .withNoEncryptionCert()
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        Metadata metadata = new Metadata(metadataCredentialResolver);
        metadata.getEncryptionPublicKey(TEST_CONNECTOR_NODE_METADATA_ENTITY_ID);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldErrorIfUnableToResolveMetadata() throws Exception {
        expectedEx.expect(ResolverException.class);
        expectedEx.expectMessage("Unable to resolve metadata credentials");

        MetadataCredentialResolver metadataResolver = mock(MetadataCredentialResolver.class);
        when(metadataResolver.resolveSingle(any())).thenThrow(ResolverException.class);

        Metadata metadata = new Metadata(metadataResolver);
        metadata.getEncryptionPublicKey(TEST_CONNECTOR_NODE_METADATA_ENTITY_ID);
    }
}
