package uk.gov.ida.notification.translator.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Support;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.common.shared.configuration.PrivateKeyConfiguration;

import java.security.cert.X509Certificate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyFileSignerConfiguration extends SignerConfiguration {
    @JsonCreator
    public KeyFileSignerConfiguration(
        @JsonProperty("publicKey") DeserializablePublicKeyConfiguration publicKey,
        @JsonProperty("privateKey") PrivateKeyConfiguration privateKey
    ) throws SignerConfigurationException {
        try {
            X509Certificate cert = X509Support.decodeCertificate(publicKey.getCert().getBytes());
            BasicX509Credential credential = new BasicX509Credential(cert, privateKey.getPrivateKey());
            this.signer = buildSigner(credential);
        } catch(Exception e) {
            throw new SignerConfigurationException(e);
        }
    }
}
