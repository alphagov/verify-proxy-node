package uk.gov.ida.notification.helpers;

import com.google.common.io.Resources;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class PKIHelpers {
    public static X509Certificate parseCert(byte[] certificateBytes) throws CertificateException {
        ByteArrayInputStream certificateStream = new ByteArrayInputStream(certificateBytes);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(certificateStream);
    }

    public static X509Certificate getCertificateFromFile(String certFile) throws CertificateException, IOException {
        return parseCert(Resources.toByteArray(Resources.getResource(certFile)));
    }

    public static PrivateKey getPrivateKeyFromFile(String privateKeyFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyAsBytes = Resources.toByteArray(Resources.getResource(privateKeyFile));
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyAsBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }

    public static Credential buildCredential(String publicKeyFile, String privateKeyFile) throws Exception {
        X509Certificate publicKeyCert = PKIHelpers.getCertificateFromFile(publicKeyFile);
        PrivateKey privateKeyCert = PKIHelpers.getPrivateKeyFromFile(privateKeyFile);
        return new BasicX509Credential(publicKeyCert, privateKeyCert);
    }
}
