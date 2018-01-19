package uk.gov.ida.notification.saml.metadata;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;

public class MetadataCredentialResolverInitializer {
    private MetadataCredentialResolver metadataCredentialResolver;
    private PredicateRoleDescriptorResolver predicateRoleDescriptorResolver;

    public MetadataCredentialResolverInitializer(MetadataResolver metadataResolver) {
        metadataCredentialResolver = new MetadataCredentialResolver();
        predicateRoleDescriptorResolver = new PredicateRoleDescriptorResolver(metadataResolver);
    }

    public MetadataCredentialResolver initialize() throws ComponentInitializationException {
        predicateRoleDescriptorResolver.initialize();
        metadataCredentialResolver.setRoleDescriptorResolver(predicateRoleDescriptorResolver);
        metadataCredentialResolver.setKeyInfoCredentialResolver(DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
        metadataCredentialResolver.initialize();
        return metadataCredentialResolver;
    }
}
