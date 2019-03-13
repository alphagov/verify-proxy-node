package uk.gov.ida.notification.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Support;

import java.io.File;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class CredentialConfigurationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void buildSignerFromKeyFileSignerConfiguration() throws Exception {
        String configJson = fixture("key_file_signer_config.yml");

        CredentialConfiguration configuration = mapper.readValue(
            String.format(configJson, getPath("test_private_key.pk8"), getPath("test_certificate.crt")),
            CredentialConfiguration.class);

        BasicX509Credential expectedCredential = new BasicX509Credential(
            X509Support.decodeCertificate(fixture("test_certificate.crt").getBytes()),
            KeySupport.decodePrivateKey(new File(getPath("test_private_key.pk8")), null)
        );

        Credential actualCredential = configuration.getCredential();

        assertThat(actualCredential.getPublicKey()).isEqualTo(expectedCredential.getPublicKey());
        assertThat(actualCredential.getPrivateKey()).isEqualTo(expectedCredential.getPrivateKey());
    }

    private String getPath(String file) {
        return Resources.getResource(file).getPath();
    }
}