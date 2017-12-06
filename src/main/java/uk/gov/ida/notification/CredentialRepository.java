package uk.gov.ida.notification;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    private final String publicKey = "local/hub_signing_primary.crt";
    private final String privateKey = "local/hub_signing_primary.pk8";
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
        URL privateKeyURL = Resources.getResource(privateKey);
        byte[] privateKeyAsBytes = Resources.toByteArray(privateKeyURL);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyAsBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(privateKeyAlgortihm);
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }

    private X509Certificate buildPublicKey() throws CertificateException, IOException {
        URL publicKeyURL = Resources.getResource(publicKey);
        InputStream publicKeyInputStream = new ByteArrayInputStream(Resources.toByteArray(publicKeyURL));
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate publicKeyCert = (X509Certificate) certificateFactory.generateCertificate(publicKeyInputStream);
        publicKeyInputStream.close();
        return publicKeyCert;
    }
}
