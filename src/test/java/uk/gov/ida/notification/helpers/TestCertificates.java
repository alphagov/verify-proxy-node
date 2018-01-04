package uk.gov.ida.notification.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class TestCertificates {

    private static final String X509 = "X.509";
    private static final String TEST_CERTIFICATE_FILE = "test_certificate.crt";

    public static X509Certificate aX509Certificate() throws CertificateException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
        String certString = FileHelpers.readFileAsString(TEST_CERTIFICATE_FILE);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(certString.getBytes(StandardCharsets.UTF_8));
        return (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
    }
}
