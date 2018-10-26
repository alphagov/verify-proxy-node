package uk.gov.ida.notification.pki;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.common.shared.configuration.PrivateKeyConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class KeyPairConfiguration {
    @NotNull
    @Valid
    @JsonProperty
    private DeserializablePublicKeyConfiguration publicKey;

    @NotNull
    @Valid
    @JsonProperty
    private PrivateKeyConfiguration privateKey;

    protected KeyPairConfiguration() {}

    public KeyPairConfiguration(DeserializablePublicKeyConfiguration publicKey, PrivateKeyConfiguration privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public DeserializablePublicKeyConfiguration getPublicKey() {
        return publicKey;
    }

    public PrivateKeyConfiguration getPrivateKey() {
        return privateKey;
    }
}
