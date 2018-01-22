package uk.gov.ida.notification.helpers;

import io.dropwizard.testing.ResourceHelpers;
import org.apache.xml.security.exceptions.Base64DecodingException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class TestCertificates {

    private static final String X509 = "X.509";
    private static final String RSA = "RSA";
    private static final String TEST_CERTIFICATE_FILE = "test_certificate.crt";
    private static final String TEST_PRIVATE_KEY_FILE = "test_private_key.pk8";

    public static X509Certificate aX509Certificate() throws CertificateException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
        String certString = FileHelpers.readFileAsString(TEST_CERTIFICATE_FILE);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(certString.getBytes(StandardCharsets.UTF_8));
        return (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
    }

    public static PrivateKey aPrivateKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, Base64DecodingException {
        Path path = Paths.get(ResourceHelpers.resourceFilePath(TEST_PRIVATE_KEY_FILE));
        byte[] bytes = Files.readAllBytes(path);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

}
