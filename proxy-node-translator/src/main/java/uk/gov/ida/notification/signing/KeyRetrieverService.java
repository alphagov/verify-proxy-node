package uk.gov.ida.notification.signing;

import uk.gov.ida.notification.TranslatorConfiguration;
import uk.gov.ida.notification.saml.SamlObjectSigner;

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