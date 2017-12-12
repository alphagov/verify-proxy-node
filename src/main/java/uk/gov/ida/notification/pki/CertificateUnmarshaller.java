package uk.gov.ida.notification.pki;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateUnmarshaller {
    private final CertificateFactory certificateFactory;

    public CertificateUnmarshaller() throws CertificateException {
        certificateFactory = CertificateFactory.getInstance("X.509");
    }

    public X509Certificate unmarshall(String cert) {
        try (ByteArrayInputStream publicKeyInputStream = new ByteArrayInputStream(cert.getBytes())) {
            return (X509Certificate) certificateFactory.generateCertificate(publicKeyInputStream);
        } catch (IOException | CertificateException e) {
            throw new CertificateUnmarshallingException(e);
        }
    }
}
