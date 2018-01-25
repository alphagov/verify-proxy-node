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

public class ConnectorNodeMetadataTest {

    private static final String TEST_CONNECTOR_NODE_METADATA_FILE = "connector_node_metadata_template.xml";
    private static final String CONNECTOR_NODE_METADATA_ENTITY_ID = "http://connector-node:8080/ConnectorResponderMetadata";

    @Test
    public void shouldReturnConnectorNodeEncryptionPublicKeyFromMetadata() throws Exception {
        X509Certificate encryptionCert = new TestKeyPair().certificate;
        PublicKey expectedPublicKey = encryptionCert.getPublicKey();

        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_CONNECTOR_NODE_METADATA_FILE)
                .withEncryptionCert(encryptionCert)
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        ConnectorNodeMetadata connectorNodeMetadata = new ConnectorNodeMetadata(metadataCredentialResolver, CONNECTOR_NODE_METADATA_ENTITY_ID);
        PublicKey connectorNodeEncryptionPublicKey = connectorNodeMetadata.getEncryptionPublicKey();

        assertEquals(expectedPublicKey, connectorNodeEncryptionPublicKey);
    }

    @Test(expected = MissingMetadataException.class)
    public void shouldErrorIfEncryptionPublicKeyElementIsEmpty() throws Exception {
        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_CONNECTOR_NODE_METADATA_FILE)
                .withEncryptionCert("")
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        ConnectorNodeMetadata connectorNodeMetadata = new ConnectorNodeMetadata(metadataCredentialResolver, CONNECTOR_NODE_METADATA_ENTITY_ID);
        connectorNodeMetadata.getEncryptionPublicKey();
    }

    @Test(expected = MissingMetadataException.class)
    public void shouldErrorIfNoEncryptionPublicKeyElement() throws Exception {
        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_CONNECTOR_NODE_METADATA_FILE)
                .withNoEncryptionCert()
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        ConnectorNodeMetadata connectorNodeMetadata = new ConnectorNodeMetadata(metadataCredentialResolver, CONNECTOR_NODE_METADATA_ENTITY_ID);
        connectorNodeMetadata.getEncryptionPublicKey();
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldErrorIfUnableToResolveMetadata() throws Exception {
        expectedEx.expect(ResolverException.class);
        expectedEx.expectMessage("Unable to resolve metadata credentials");

        MetadataCredentialResolver metadataResolver = mock(MetadataCredentialResolver.class);
        when(metadataResolver.resolveSingle(any())).thenThrow(ResolverException.class);

        ConnectorNodeMetadata connectorNodeMetadata = new ConnectorNodeMetadata(metadataResolver, CONNECTOR_NODE_METADATA_ENTITY_ID);
        connectorNodeMetadata.getEncryptionPublicKey();
    }
}