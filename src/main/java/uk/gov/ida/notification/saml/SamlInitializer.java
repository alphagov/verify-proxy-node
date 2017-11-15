package uk.gov.ida.notification.saml;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.Initializer;

public class SamlInitializer implements Initializer {
    @Override
    public void init() throws InitializationException {
        // Initialise Verify SAML extensions here
        // For example:
        //   XMLObjectProviderRegistrySupport.registerObjectProvider(SPType.DEFAULT_ELEMENT_NAME, new SPTypeBuilder(), new SPTypeMarshaller(), new SPTypeUnmarshaller());
    }
}
