package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.notification.helpers.PKIHelpers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CredentialRepositoryTest {
    private EidasProxyNodeConfiguration configuration = mock(EidasProxyNodeConfiguration.class);
    private static CredentialRepository credentialRepository;
    private final String hubSigningPrivateKeyPath = "local/hub_signing_primary.pk8";
    private final String hubSigningCertificatePath = "local/hub_signing_primary.crt";

    @Test
    public void shouldReturnHubCredentials() throws Throwable {
        setupResourceDependenciesForSuccess();
        credentialRepository = new CredentialRepository(hubSigningPrivateKeyPath, hubSigningCertificatePath);

        Credential credential = credentialRepository.getHubCredential();

        Credential expectedCredential = PKIHelpers.buildCredential(hubSigningCertificatePath, hubSigningPrivateKeyPath);
        assertEquals(expectedCredential.getPrivateKey(), credential.getPrivateKey());
        assertEquals(expectedCredential.getPublicKey(), credential.getPublicKey());
        assertEquals(expectedCredential.getEntityId(), credential.getEntityId());
    }

    private void setupResourceDependenciesForSuccess() throws Throwable {
        when(configuration.getHubSigningPrivateKeyPath()).thenReturn((hubSigningPrivateKeyPath));
        when(configuration.getHubSigningCertificatePath()).thenReturn((hubSigningCertificatePath));
    }
}
