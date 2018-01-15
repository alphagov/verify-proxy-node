package uk.gov.ida.notification.pki;

import org.opensaml.security.credential.BasicCredential;

import java.security.PrivateKey;
import java.security.PublicKey;

public class DecryptionCredential extends BasicCredential {
    public DecryptionCredential(PublicKey publicKey, PrivateKey privateKey) {
        super(publicKey, privateKey);
    }
}
