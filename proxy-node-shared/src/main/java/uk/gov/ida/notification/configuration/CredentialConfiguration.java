package uk.gov.ida.notification.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

import java.security.interfaces.ECPublicKey;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value= KeyFileCredentialConfiguration.class, name="file"),
    @JsonSubTypes.Type(value= CloudHsmCredentialConfiguration.class, name="cloudhsm")
})
public abstract class CredentialConfiguration {
    protected BasicX509Credential credential;
    protected String algorithm;
    protected Long keyHandle;

    public BasicX509Credential getCredential() { return credential; }
    public String getAlgorithm() { return algorithm; }

    public void setCredential(BasicX509Credential credential) {
        this.algorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        if (credential.getPublicKey() instanceof ECPublicKey) {
            this.algorithm = SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256;
        }
        this.credential = credential;
    }

    public Long getKeyHandle() {
        return this.keyHandle;
    }
}
