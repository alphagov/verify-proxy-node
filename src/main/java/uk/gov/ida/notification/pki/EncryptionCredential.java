package uk.gov.ida.notification.pki;

import org.opensaml.security.credential.BasicCredential;

import java.security.PublicKey;

public class EncryptionCredential extends BasicCredential {
    public EncryptionCredential(PublicKey publicKey) { super(publicKey); }
}
