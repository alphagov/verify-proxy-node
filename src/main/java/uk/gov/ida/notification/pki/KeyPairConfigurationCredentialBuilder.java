package uk.gov.ida.notification.pki;

import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyPairConfigurationCredentialBuilder {
    public SigningCredential buildSigningCredential(KeyPairConfiguration keyPairConfiguration) {
        PrivateKey privateKey = keyPairConfiguration.getPrivateKey().getPrivateKey();
        PublicKey publicKey = keyPairConfiguration.getPublicKey().getPublicKey();
        return new SigningCredential(publicKey, privateKey);
    }
}
