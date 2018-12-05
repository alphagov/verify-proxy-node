package uk.gov.ida.notification.saml.metadata;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;

public class MetadataFactory {

    public static Metadata createMetadataFromBundle(MetadataResolverBundle bundle) throws ComponentInitializationException {
        MetadataCredentialResolver metadataCredentialResolver = new MetadataCredentialResolverInitializer(bundle.getMetadataResolver()).initialize();
        return new Metadata(metadataCredentialResolver);
    }
}
