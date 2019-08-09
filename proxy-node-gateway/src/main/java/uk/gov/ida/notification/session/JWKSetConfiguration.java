package uk.gov.ida.notification.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import uk.gov.ida.notification.exceptions.JSONWebTokenException;

import java.security.KeyPair;

public class JWKSetConfiguration {

    private final transient KeyPair encryptionKeyPair;
    private final transient KeyPair signingKeyPair;

    @JsonCreator
    public JWKSetConfiguration(@JsonProperty("jwkSet") String jwkSetString) {
        try {
            JWKSet jwkSet = JWKSet.parse(jwkSetString);
            this.encryptionKeyPair = getKey(jwkSet, KeyUse.ENCRYPTION).toKeyPair();
            this.signingKeyPair = getKey(jwkSet, KeyUse.SIGNATURE).toKeyPair();
        } catch (Exception e) {
            throw new JSONWebTokenException("Cannot parse provided jwkSet", e);
        }
    }

    public KeyPair getEncryptionKeyPair() {
        return encryptionKeyPair;
    }

    public KeyPair getSigningKeyPair() {
        return signingKeyPair;
    }

    private RSAKey getKey(JWKSet jwkSet, KeyUse keyUse) {
        for (JWK key : jwkSet.getKeys()) {
            if (keyUse == key.getKeyUse()) {
                return (RSAKey) key;
            }
        }
        throw new JSONWebTokenException(String.format("Could not find jwk for use of %s", keyUse));
    }
}
