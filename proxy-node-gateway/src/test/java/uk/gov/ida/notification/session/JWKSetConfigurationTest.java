package uk.gov.ida.notification.session;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

public class JWKSetConfigurationTest {

    @Test
    public void testValidKeyPairsCanBeExtractedFromJWKSet() throws Exception {
        String path = getClass().getClassLoader().getResource("jws-set.json").getPath();
        final String expectedMetadata = new String(Files.readAllBytes(Paths.get(path)));
        JWKSetConfiguration configuration = new JWKSetConfiguration(expectedMetadata);
        KeyPair encryptionKeyPair = configuration.getEncryptionKeyPair();
        KeyPair signingKeyPair = configuration.getSigningKeyPair();
        assertThat(testMatch(encryptionKeyPair.getPublic(), encryptionKeyPair.getPrivate())).isTrue();
        assertThat(testMatch(signingKeyPair.getPublic(), signingKeyPair.getPrivate())).isTrue();
        assertThat(testMatch(encryptionKeyPair.getPublic(), signingKeyPair.getPrivate())).isFalse();
        assertThat(testMatch(signingKeyPair.getPublic(), encryptionKeyPair.getPrivate())).isFalse();
    }

    private boolean testMatch(PublicKey publicKey, PrivateKey privateKey) throws Exception {
        byte[] challenge = new byte[2000];
        ThreadLocalRandom.current().nextBytes(challenge);
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(challenge);
        byte[] signature = sig.sign();
        sig.initVerify(publicKey);
        sig.update(challenge);
        return sig.verify(signature);
    }
}