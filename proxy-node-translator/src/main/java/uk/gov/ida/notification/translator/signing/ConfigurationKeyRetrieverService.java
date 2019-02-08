package uk.gov.ida.notification.translator.signing;

import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.translator.TranslatorConfiguration;

/**
 * Retrieves ConnectorFacingSigningKeyPair from Dropwizard Configuration
 */
public final class ConfigurationKeyRetrieverService extends KeyRetrieverService {

    private ConfigurationKeyRetrieverService() {
    }

    public ConfigurationKeyRetrieverService(TranslatorConfiguration configuration) {
        setConfiguration(configuration);
    }

    @Override
    public SamlObjectSigner createSamlObjectSigner() {
        return new SamlObjectSigner(
                getConfiguration().getConnectorFacingSigningKeyPair().getPublicKey().getPublicKey(),
                getConfiguration().getConnectorFacingSigningKeyPair().getPrivateKey().getPrivateKey(),
                getConfiguration().getConnectorFacingSigningKeyPair().getPublicKey().getCert()
        );
    }
}
