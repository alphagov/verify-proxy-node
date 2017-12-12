package uk.gov.ida.notification.pki;

import org.junit.Test;
import uk.gov.ida.notification.helpers.FileHelpers;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class CertificateUnmarshallerTest {
    @Test
    public void shouldUnmarshallX509CertificateFromString() throws Throwable {
        CertificateUnmarshaller certificateUnmarshaller = new CertificateUnmarshaller();
        X509Certificate certificate = certificateUnmarshaller.unmarshall(FileHelpers.readFileAsString("test_signing.crt"));

        assertEquals("CN=proxynode signing, OU=Verify, O=GDS, L=LO, ST=EN, C=UK", certificate.getSubjectDN().getName());
    }

    @Test
    public void shouldThrowExceptionWithInvalidString() throws Throwable {
        CertificateUnmarshaller certificateUnmarshaller = new CertificateUnmarshaller();
        String certString = "wrong";

        try {
            certificateUnmarshaller.unmarshall(certString);
        } catch(Exception e) {
            assertThat(e).isInstanceOf(CertificateUnmarshallingException.class);
            assertThat(e.getCause()).isInstanceOf(CertificateException.class);
            assertThat(e.getMessage()).contains("Failed to unmarshall certificate");
        }
    }
}
