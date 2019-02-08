package uk.gov.ida.notification.translator.signing;

import se.swedenconnect.opensaml.pkcs11.PKCS11Provider;
import se.swedenconnect.opensaml.pkcs11.PKCS11ProviderFactory;
import se.swedenconnect.opensaml.pkcs11.configuration.PKCS11ProviderConfiguration;
import se.swedenconnect.opensaml.pkcs11.configuration.PKCS11SoftHsmProviderConfiguration;
import se.swedenconnect.opensaml.pkcs11.providerimpl.PKCS11ProviderInstance;

import java.security.Provider;
import java.security.Security;

public class PKCS11ProviderHelper {

    // PKCS11 Constants
    private static final String PROVIDER_SUN_PKCS_11 = "SunPKCS11";

    public static PKCS11Provider createPKCS11ProviderForConfiguration(PKCS11ProviderConfiguration configuration) throws Exception {

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

    public static PKCS11ProviderConfiguration createPKCS11SoftHSMProviderConfigurationWithoutCredentials(String softHSMLibPath, String alias, String pin) {

        PKCS11SoftHsmProviderConfiguration configuration
                = new PKCS11SoftHsmProviderConfiguration();

        configuration.setName(alias);
        configuration.setLibrary(softHSMLibPath);
        configuration.setPin(pin);

        return configuration;
    }

}
