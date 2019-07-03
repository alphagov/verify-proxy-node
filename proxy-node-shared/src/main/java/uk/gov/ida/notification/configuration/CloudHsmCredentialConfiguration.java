package uk.gov.ida.notification.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.xml.security.algorithms.JCEMapper;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Support;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;

import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Optional;

import static java.text.MessageFormat.format;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudHsmCredentialConfiguration extends CredentialConfiguration {
    public static final String ID = "CloudHsmCredentialConfiguration";

    @JsonCreator
    public CloudHsmCredentialConfiguration(
            @JsonProperty("publicKey") DeserializablePublicKeyConfiguration publicKey,
            @JsonProperty("hsmKeyLabel") String hsmKeyLabel
    ) throws CredentialConfigurationException {
        try {
            ProxyNodeLogger.info(format("Using CloudHsmCredentialConfiguration to sign eIDAS responses with HSM Key Label {0}", hsmKeyLabel));

            X509Certificate certificate = Optional
                    .ofNullable(X509Support.decodeCertificate(publicKey.getCert().getBytes()))
                    .orElseThrow(() -> new CredentialConfigurationException("Could not decode public certificate"));

            Provider caviumProvider = (Provider) ClassLoader.getSystemClassLoader()
                    .loadClass("com.cavium.provider.CaviumProvider")
                    .getConstructor()
                    .newInstance();
            Security.addProvider(caviumProvider);
            JCEMapper.setProviderId("Cavium");
            KeyStore cloudHsmStore = KeyStore.getInstance("Cavium");
            cloudHsmStore.load(null, null);

            final Key key = cloudHsmStore.getKey(hsmKeyLabel, null);
            BasicX509Credential credential = new BasicX509Credential(
                    certificate,
                    (PrivateKey) key);

            this.keyHandle = (Long) ClassLoader.getSystemClassLoader()
                    .loadClass("com.cavium.key.CaviumKey")
                    .getMethod("getHandle")
                    .invoke(key);

            credential.setEntityId(ID);
            setCredential(credential);
        } catch (Exception e) {
            throw new CredentialConfigurationException(e);
        }
    }
}
