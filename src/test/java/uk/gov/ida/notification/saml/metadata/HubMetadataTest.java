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

public class HubMetadataTest {

    private static final String TEST_HUB_METADATA_FILE = "hub_metadata_template.xml";
    private static final String HUB_METADATA_ENTITY_ID = "http://hub:8080/HubResponderMetadata";

    @Test
    public void shouldReturnHubSigningPublicKeyFromMetadata() throws Exception {
        X509Certificate signingCert = new TestKeyPair().certificate;
        PublicKey expectedPublicKey = signingCert.getPublicKey();

        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_HUB_METADATA_FILE)
                .withSigningCert(signingCert)
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        HubMetadata hubMetadata = new HubMetadata(metadataCredentialResolver, HUB_METADATA_ENTITY_ID);
        PublicKey hubSigningPublicKey = hubMetadata.getSigningPublicKey();

        assertEquals(expectedPublicKey, hubSigningPublicKey);
    }

    @Test(expected = MissingMetadataException.class)
    public void shouldErrorIfSigningPublicKeyElementIsEmpty() throws Exception {
        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_HUB_METADATA_FILE)
                .withSigningCert("")
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        HubMetadata hubMetadata = new HubMetadata(metadataCredentialResolver, HUB_METADATA_ENTITY_ID);
        hubMetadata.getSigningPublicKey();
    }

    @Test(expected = MissingMetadataException.class)
    public void shouldErrorIfNoSigningPublicKeyElement() throws Exception {
        MetadataResolver metadataResolver = new TestMetadataBuilder(TEST_HUB_METADATA_FILE)
                .withNoSigningCert()
                .buildResolver("someId");
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverBuilder(metadataResolver).build();

        HubMetadata hubMetadata = new HubMetadata(metadataCredentialResolver, HUB_METADATA_ENTITY_ID);
        hubMetadata.getSigningPublicKey();
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldErrorIfUnableToResolveMetadata() throws Exception {
        expectedEx.expect(ResolverException.class);
        expectedEx.expectMessage("Unable to resolve metadata credentials");

        MetadataCredentialResolver metadataResolver = mock(MetadataCredentialResolver.class);
        when(metadataResolver.resolveSingle(any())).thenThrow(ResolverException.class);

        HubMetadata hubMetadata = new HubMetadata(metadataResolver, HUB_METADATA_ENTITY_ID);
        hubMetadata.getSigningPublicKey();
    }
}