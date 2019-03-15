package uk.gov.ida.notification.configuration;

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
public class KeyFileCredentialConfiguration extends CredentialConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(KeyFileCredentialConfiguration.class);

    @JsonCreator
    public KeyFileCredentialConfiguration(
        @JsonProperty("publicKey") DeserializablePublicKeyConfiguration publicKey,
        @JsonProperty("privateKey") PrivateKeyConfiguration privateKey
    ) throws CredentialConfigurationException {
        try {
            LOG.info("Signing eIDAS with KeyFileCredentialConfiguration");
            X509Certificate cert = X509Support.decodeCertificate(publicKey.getCert().getBytes());
            BasicX509Credential credential = new BasicX509Credential(cert, privateKey.getPrivateKey());
            credential.setEntityId("KeyFileCredentialConfiguration");
            setCredential(credential);
        } catch(Exception e) {
            throw new CredentialConfigurationException(e);
        }
    }
}
