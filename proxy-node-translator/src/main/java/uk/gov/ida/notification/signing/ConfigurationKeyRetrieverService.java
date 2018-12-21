package uk.gov.ida.notification.signing;

import uk.gov.ida.notification.TranslatorConfiguration;
import uk.gov.ida.notification.saml.SamlObjectSigner;

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