package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CredentialRepositoryTest {
    private EidasProxyNodeConfiguration configuration = mock(EidasProxyNodeConfiguration.class);
    private static CredentialRepository credentialRepository;
    private final String privateKey = "pki/hub_signing.pk8";
    private final String publicKey = "pki/hub_signing.crt";

    @Test
    public void shouldReturnHubCredentials() throws Throwable {
        setupResourceDependenciesForSuccess();

        credentialRepository = new CredentialRepository(privateKey, publicKey);

        Credential credential = credentialRepository.getHubCredential();

        Credential expectedCredential = buildCredential();
        assertEquals(expectedCredential.getPrivateKey(), credential.getPrivateKey());
        assertEquals(expectedCredential.getPublicKey(), credential.getPublicKey());
        assertEquals(expectedCredential.getEntityId(), credential.getEntityId());
    }

    private void setupResourceDependenciesForSuccess() throws Throwable {
        when(configuration.getHubSigningPrivateKeyPath()).thenReturn((privateKey));
        when(configuration.getHubSigningCertificatePath()).thenReturn((publicKey));
    }

    private Credential buildCredential() throws Throwable {
        String publicKey = "src/main/resources/pki/hub_signing.crt";
        String privateKey = "src/main/resources/pki/hub_signing.pk8";
        X509Certificate publicKeyCert = buildPublicKey(publicKey);
        PrivateKey privateKeyCert = buildPrivateKey(privateKey);
        return new BasicX509Credential(publicKeyCert, privateKeyCert);
    }

    private X509Certificate buildPublicKey(String publicKey) throws CertificateException, IOException {
        InputStream publicKeyInputStream = new FileInputStream(publicKey);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate publicKeyCert = (X509Certificate) certificateFactory.generateCertificate(publicKeyInputStream);
        publicKeyInputStream.close();
        return publicKeyCert;
    }

    private PrivateKey buildPrivateKey(String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        RandomAccessFile raf = new RandomAccessFile(privateKey, "r");
        byte[] privateKeyAsBytes = new byte[(int)raf.length()];
        raf.readFully(privateKeyAsBytes);
        raf.close();
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyAsBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }
}
