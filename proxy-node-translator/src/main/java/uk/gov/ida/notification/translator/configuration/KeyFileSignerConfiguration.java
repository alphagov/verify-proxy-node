package uk.gov.ida.notification.translator.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Support;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.common.shared.configuration.PrivateKeyConfiguration;

import java.security.cert.X509Certificate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyFileSignerConfiguration extends SignerConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(KeyFileSignerConfiguration.class);

    @JsonCreator
    public KeyFileSignerConfiguration(
        @JsonProperty("publicKey") DeserializablePublicKeyConfiguration publicKey,
        @JsonProperty("privateKey") PrivateKeyConfiguration privateKey
    ) throws SignerConfigurationException {
        try {
            LOG.info(String.format("Signing eIDAS with KeyFile with Public Cert %s", publicKey.getCert()));
            X509Certificate cert = X509Support.decodeCertificate(publicKey.getCert().getBytes());
            BasicX509Credential credential = new BasicX509Credential(cert, privateKey.getPrivateKey());
            credential.setEntityId("KeyFileSignerConfiguration");
            this.signer = buildSigner(credential);
        } catch(Exception e) {
            throw new SignerConfigurationException(e);
        }
    }
}
