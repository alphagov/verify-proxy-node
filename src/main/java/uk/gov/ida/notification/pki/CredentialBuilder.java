package uk.gov.ida.notification.pki;

import java.security.PrivateKey;
import java.security.PublicKey;

public class CredentialBuilder {
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public SigningCredential buildSigningCredential() {
        return new SigningCredential(publicKey, privateKey);
    }

    public DecryptingCredential buildDecryptingCredential() {
        return new DecryptingCredential(publicKey, privateKey);
    }

    public static CredentialBuilder withKeyPairConfiguration(KeyPairConfiguration keyPairConfiguration) {
        return new CredentialBuilder(
                keyPairConfiguration.getPublicKey().getPublicKey(),
                keyPairConfiguration.getPrivateKey().getPrivateKey()
        );
    }

    private CredentialBuilder(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
}
