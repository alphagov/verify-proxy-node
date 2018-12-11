package uk.gov.ida.notification.softhsm;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.security.credential.Credential;
import se.swedenconnect.opensaml.pkcs11.PKCS11Provider;
import se.swedenconnect.opensaml.pkcs11.PKCS11ProviderFactory;
import se.swedenconnect.opensaml.pkcs11.configuration.PKCS11ProviderConfiguration;
import se.swedenconnect.opensaml.pkcs11.configuration.PKCS11SoftHsmProviderConfiguration;
import se.swedenconnect.opensaml.pkcs11.configuration.SoftHsmCredentialConfiguration;
import se.swedenconnect.opensaml.pkcs11.credential.PKCS11Credential;
import se.swedenconnect.opensaml.pkcs11.providerimpl.PKCS11ProviderInstance;
import uk.gov.ida.notification.helpers.TestKeyPair;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

/**
 * Tests demonstrating usage of the swedenconnect opensaml-pkcs11 library in conjunction with softHSM.
 * https://github.com/swedenconnect/opensaml-pkcs11
 * https://wiki.opendnssec.org/display/SoftHSM/Home
 * <p>
 * SoftHSM must be installed locally in order for the tests to run.
 */
public class SoftHSMIntegrationTest {

    // Test Certificate Constants
    private static final String PROXY_NODE_SIGNING_PK_8 = "proxy_node_signing.pk8";
    private static final String PROXY_NODE_SIGNING_CRT = "proxy_node_signing.crt";

    // Test PKCS11 Constants
    private static final String PROVIDER_SUN_PKCS_11 = "SunPKCS11";

    // Test Credential Constants
    private static final String CRED_ALIAS_PREFIX = "vfpn-uk";
    private static final String CRED_ENV = "dev";
    private static final String CRED_PIN = "1234";
    private static final String CRED_SLOT = "0";
    private static final String CRED_NAME = "vfpn-uk";
    private static final String CRED_ALIAS = CRED_ALIAS_PREFIX + "-" + CRED_ENV;

    // softHSM Configuration
    private static String softHSMLibPath = System.getenv("SOFT_HSM_LIB_PATH");

    /**
     * softHSM tests should only run if softHSM is installed and configured.
     * Installation and configuration to be indicated by the SOFT_HSM_LIB_PATH environment variable.
     * softHSM is installed in the Translator Docker image and this variable is set, so the tests
     * will run on pre-commit.  Running the tests when building locally is effectively excluded
     * by not setting this environment variable.
     */
    @Before
    public void checkSoftHSMConfigured() {
        assumeTrue(softHSMLibPath != null && softHSMLibPath.length() > 0);
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void shouldThrowExceptionForPKCS11ProviderConfigurationWithoutCredentials() throws Exception {

        PKCS11ProviderConfiguration configuration = createPKCS11SoftHSMProviderConfigurationWithoutCredentials(CRED_SLOT, CRED_NAME);
        PKCS11Provider pkcs11Provider = getPkcs11ProviderForConfiguration(configuration);

        TestKeyPair signingTestKeyPair = new TestKeyPair(PROXY_NODE_SIGNING_CRT, PROXY_NODE_SIGNING_PK_8);
        Credential credential = new PKCS11Credential(
                signingTestKeyPair.certificate,
                pkcs11Provider.getProviderNameList(),
                CRED_ALIAS_PREFIX, CRED_PIN);
    }

    @Test
    public void shouldCreateCredentialConfigurationList() {
        PKCS11ProviderConfiguration configuration = createPKCS11SoftHSMProviderConfigurationWithCredentials(CRED_ALIAS, CRED_ENV, CRED_PIN, CRED_SLOT, CRED_NAME);
        assertNotNull("PKCS11ProviderConfiguration not loaded from credentials", configuration);
    }


    @Test
    public void shouldCreatePKSC11ProviderFromFactory() throws Exception {

        PKCS11ProviderConfiguration configuration = createPKCS11SoftHSMProviderConfigurationWithCredentials(CRED_ALIAS, CRED_ENV, CRED_PIN, CRED_SLOT, CRED_NAME);
        PKCS11Provider pkcs11Provider = getPkcs11ProviderForConfiguration(configuration);
        assertNotNull(pkcs11Provider);
    }


    @Test
    public void shouldRetrieveCredentialsFromHSM() throws Exception {

        PKCS11ProviderConfiguration configuration = createPKCS11SoftHSMProviderConfigurationWithCredentials(CRED_ALIAS, CRED_ENV, CRED_PIN, "162624334", CRED_NAME);
        PKCS11Provider pkcs11Provider = getPkcs11ProviderForConfiguration(configuration);
        TestKeyPair signingTestKeyPair = new TestKeyPair(PROXY_NODE_SIGNING_CRT, PROXY_NODE_SIGNING_PK_8);

        addPrivateKeyToKeyStore(signingTestKeyPair, CRED_ALIAS, CRED_PIN);

        Credential credential = new PKCS11Credential(
                signingTestKeyPair.certificate,
                pkcs11Provider.getProviderNameList(),
                CRED_ALIAS, CRED_PIN);

        assertNotNull(credential.getPrivateKey());
    }

    private void addPrivateKeyToKeyStore(TestKeyPair signingTestKeyPair, String alias, String pin) throws KeyStoreException, NoSuchProviderException, IOException, NoSuchAlgorithmException, CertificateException {

        KeyStore keyStore = KeyStore.getInstance("PKCS11", PROVIDER_SUN_PKCS_11 + "-" + alias);
        keyStore.load((InputStream) null, pin.toCharArray());

        Certificate[] chain = {signingTestKeyPair.certificate};

        keyStore.setKeyEntry(alias, signingTestKeyPair.privateKey, pin.toCharArray(), chain);
        keyStore.store(null);
    }


    private PKCS11Provider getPkcs11ProviderForConfiguration(PKCS11ProviderConfiguration configuration) throws Exception {

        PKCS11ProviderFactory factory = new PKCS11ProviderFactory(configuration,
                new PKCS11ProviderInstance() {
                    @Override
                    public Provider getProviderInstance(String configString) {
                        Provider sunPKCS11 = Security.getProvider(PROVIDER_SUN_PKCS_11);
                        // In Java 9+ in-line config data preceded with "--" (or else treated as file path).
                        sunPKCS11 = sunPKCS11.configure("--" + configString);
                        return sunPKCS11;
                    }
                });
        return factory.createInstance();
    }

    private PKCS11ProviderConfiguration createPKCS11SoftHSMProviderConfigurationWithCredentials(String alias, String environment, String pin, String slot, String name) {

        PKCS11SoftHsmProviderConfiguration configuration
                = new PKCS11SoftHsmProviderConfiguration();
        List<SoftHsmCredentialConfiguration> credentials = new ArrayList<>();
        credentials.add(new SoftHsmCredentialConfiguration(
                alias,
                PROXY_NODE_SIGNING_PK_8,
                PROXY_NODE_SIGNING_CRT));
        configuration.setPin(pin);
        configuration.setSlot(slot);
        configuration.setName(alias);
        configuration.setCredentialConfigurationList(credentials);
        configuration.setLibrary(softHSMLibPath);
        return configuration;
    }

    private PKCS11ProviderConfiguration createPKCS11SoftHSMProviderConfigurationWithoutCredentials(String slot, String name) {

        PKCS11ProviderConfiguration configuration
                = new PKCS11SoftHsmProviderConfiguration();
        configuration.setName(name);
        configuration.setLibrary(softHSMLibPath);
        configuration.setSlot(slot);
        configuration.setSlotListIndexMaxRange(4);
        return configuration;
    }

}