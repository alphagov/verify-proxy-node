package uk.gov.ida.notification.pki;

import java.security.PrivateKey;
import java.security.PublicKey;

public class CredentialBuilder {
    private PublicKey publicKey;
    private PrivateKey privateKey;

    private CredentialBuilder(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public static CredentialBuilder withKeyPairConfiguration(KeyPairConfiguration keyPairConfiguration) {
        return new CredentialBuilder(
                keyPairConfiguration.getPublicKey().getPublicKey(),
                keyPairConfiguration.getPrivateKey().getPrivateKey()
        );
    }

    public static CredentialBuilder withPublicKey(PublicKey publicKey) {
        return new CredentialBuilder(
                publicKey,
                null
        );
    }

    public SigningCredential buildSigningCredential(String certString) {
        if (privateKey == null) {
            throw new RuntimeException("Cannot build signing credential: private key is null");
        }
        return new SigningCredential(publicKey, privateKey, certString);
    }

    public DecryptionCredential buildDecryptionCredential() {
        if (privateKey == null) {
            throw new RuntimeException("Cannot build decryption credential: private key is null");
        }
        return new DecryptionCredential(publicKey, privateKey);
    }
}
