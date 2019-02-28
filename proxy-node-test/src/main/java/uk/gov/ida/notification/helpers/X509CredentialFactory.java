package uk.gov.ida.notification.helpers;

import org.opensaml.security.crypto.KeySupport;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Support;

public class X509CredentialFactory {
    public static BasicX509Credential build(String cert, String key) throws Exception {
        String fullCert = String.format("-----BEGIN CERTIFICATE-----\n%s\n-----END CERTIFICATE-----", cert);
        return new BasicX509Credential(
            X509Support.decodeCertificate(fullCert.getBytes()),
            KeySupport.decodePrivateKey(key.getBytes(), null)
        );
    }
}
