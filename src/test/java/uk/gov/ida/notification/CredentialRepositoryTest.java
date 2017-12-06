package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.security.credential.Credential;

import static org.junit.Assert.assertEquals;
import static uk.gov.ida.notification.helpers.PKIHelpers.buildCredential;

public class CredentialRepositoryTest {
    @Test
    public void shouldReturnHubCredentials() throws Throwable {
        CredentialRepository credentialRepository = new CredentialRepository();

        Credential credential = credentialRepository.getHubCredential();

        Credential expectedCredential = buildCredential(
                "local/hub_signing_primary.crt",
                "local/hub_signing_primary.pk8");

        assertEquals(credential.getPrivateKey(), expectedCredential.getPrivateKey());
        assertEquals(credential.getPublicKey(), expectedCredential.getPublicKey());
        assertEquals(credential.getEntityId(), expectedCredential.getEntityId());
    }
}
