package uk.gov.ida.notification.translator.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Support;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;

public class CloudHsmSignerConfiguration extends SignerConfiguration {
    @JsonCreator
    public CloudHsmSignerConfiguration(
        @JsonProperty("base64cert") String base64cert,
        @JsonProperty("keyLabel") String keyLabel
    ) throws SignerConfigurationException {
        try {
            X509Certificate certificate = X509Support.decodeCertificate(base64cert);
            Provider caviumProvider = (Provider) ClassLoader.getSystemClassLoader()
                .loadClass("com.cavium.provider.CaviumProvider")
                .getConstructor()
                .newInstance();
            Security.addProvider(caviumProvider);
            KeyStore cloudHsmStore = KeyStore.getInstance("Cavium");
            cloudHsmStore.load(null, null);
            BasicX509Credential credential = new BasicX509Credential(certificate, (PrivateKey) cloudHsmStore.getKey(keyLabel, null));
            this.signer = buildSigner(credential);
        } catch(Exception e) {
            throw new SignerConfigurationException(e);
        }
    }
}
