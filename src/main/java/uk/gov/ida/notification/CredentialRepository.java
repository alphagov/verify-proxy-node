package uk.gov.ida.notification;

import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class CredentialRepository {

    private final String publicKey = "src/main/resources/pki/hub_signing.crt";
    private final String privateKey = "src/main/resources/pki/hub_signing.pk8";
    private final String privateKeyAlgortihm = "RSA";

    public Credential getHubCredential() throws Throwable {
        return buildCredential();
    }
    private Credential buildCredential() throws Throwable {
        X509Certificate publicKeyCert = buildPublicKey();
        PrivateKey privateKeyCert = buildPrivateKey();
        return new BasicX509Credential(publicKeyCert, privateKeyCert);
    }

    private PrivateKey buildPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyAsBytes = Files.readAllBytes(Paths.get(privateKey));
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyAsBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(privateKeyAlgortihm);
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }

    private X509Certificate buildPublicKey() throws CertificateException, IOException {
        InputStream publicKeyInputStream = new FileInputStream(publicKey);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate publicKeyCert = (X509Certificate)certificateFactory.generateCertificate(publicKeyInputStream);
        publicKeyInputStream.close();
        return publicKeyCert;
    }
}
