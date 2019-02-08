package uk.gov.ida.notification.translator.signing;

import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.translator.configuration.TranslatorConfiguration;

public abstract class KeyRetrieverService {
    private TranslatorConfiguration configuration;

    protected final void setConfiguration(TranslatorConfiguration configuration) {
        this.configuration = configuration;
    }

    protected final TranslatorConfiguration getConfiguration() {
        return this.configuration;
    }

    public abstract SamlObjectSigner createSamlObjectSigner();
}
