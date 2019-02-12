package uk.gov.ida.notification.translator.signing;

import org.opensaml.security.credential.Credential;
import se.swedenconnect.opensaml.pkcs11.PKCS11Provider;
import se.swedenconnect.opensaml.pkcs11.configuration.PKCS11ProviderConfiguration;
import se.swedenconnect.opensaml.pkcs11.credential.PKCS11Credential;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.translator.configuration.TranslatorConfiguration;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

/**
 * Creates a Credential for signing by using the SE Connect lib to access HSM.
 */
public class SoftHSMKeyRetrieverService extends KeyRetrieverService {

    private static final Logger LOG = Logger.getLogger(SoftHSMKeyRetrieverService.class.getName());

    private static final String X509 = "X.509";

    // OpenSAML Credential for Signing
    private Credential signingCredential;

    private SoftHSMKeyRetrieverService() {
    }

    public SoftHSMKeyRetrieverService(TranslatorConfiguration configuration) {

        setConfiguration(configuration);

        try {
            PKCS11ProviderConfiguration pkcs11ProviderConfiguration =
                    PKCS11ProviderHelper.createPKCS11SoftHSMProviderConfigurationWithoutCredentials(
                            getConfiguration().getSoftHSMLibPath(),
                            getConfiguration().getSoftHSMSigningKeyLabel(),
                            getConfiguration().getSoftHSMSigningKeyPin());
            PKCS11Provider pkcs11Provider = PKCS11ProviderHelper.createPKCS11ProviderForConfiguration(pkcs11ProviderConfiguration);

            X509Certificate signingCertificate = createX509CertificateFromString(getConnectorFacingSigningCertificateAsString());

            // Must supply the Cert to retrieve the corresponding Private Key from softHSM via SE lib.
            signingCredential = new PKCS11Credential(
                    signingCertificate,
                    pkcs11Provider.getProviderNameList(),
                    getConfiguration().getSoftHSMSigningKeyLabel(),
                    getConfiguration().getSoftHSMSigningKeyPin());
        } catch (Exception e) {
            throw new KeyRetrieverException("Unable to retrieve Signing Credential from softHSM: ", e);
        }

        LOG.info("Retrieved Signing Credential from softHSM.");
    }

    private String getConnectorFacingSigningCertificateAsString() {
        /**
         * Currently still retrieving the Cert from config rather than the softHSM itself.
         * This is because it is readily available and still the same thing.
         * No OOTB way in SE pkcs11lib to get the Cert from softHSM.
         */
        return getConfiguration().getConnectorFacingSigningKeyPair().getPublicKey().getCert();
    }

    private Credential getSigningCredential() {
        return signingCredential;
    }

    @Override
    public SamlObjectSigner createSamlObjectSigner() {
        return new SamlObjectSigner(
                getSigningCredential(),
                getConnectorFacingSigningCertificateAsString()
        );
    }

    private X509Certificate createX509CertificateFromString(String certificate) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance(X509).generateCertificate(new ByteArrayInputStream(certificate.getBytes()));
    }

}
