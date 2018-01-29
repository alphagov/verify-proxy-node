package uk.gov.ida.notification.pki;

import org.opensaml.security.credential.BasicCredential;

import java.security.PrivateKey;
import java.security.PublicKey;

public class SigningCredential extends BasicCredential {
    private final String certificateString;

    public SigningCredential(PublicKey publicKey, PrivateKey privateKey, String certificateString) {
        super(publicKey, privateKey);
        this.certificateString = certificateString;
    }

    public String getCertificateString() {
        return certificateString;
    }
}
