package uk.gov.ida.notification.translator.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import uk.gov.ida.notification.saml.SamlObjectSigner;

import java.security.interfaces.ECPublicKey;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value=KeyFileSignerConfiguration.class, name="file"),
    @JsonSubTypes.Type(value=CloudHsmSignerConfiguration.class, name="cloudhsm")
})
public abstract class SignerConfiguration {
    protected SamlObjectSigner signer;

    public SamlObjectSigner getSigner() {
        return signer;
    }

    public static SamlObjectSigner buildSigner(BasicX509Credential credential) {
        String algorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        if (credential.getPublicKey() instanceof ECPublicKey) {
            algorithm = SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256;
        }
        return new SamlObjectSigner(credential, algorithm);
    }
}
