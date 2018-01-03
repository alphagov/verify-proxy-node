package uk.gov.ida.notification.saml.metadata;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;

import java.security.PublicKey;
import java.util.logging.Logger;

public class ConnectorNodeMetadata {
    private final MetadataCredentialResolver metadataCredentialResolver;
    private final String entityId;
    private final Logger LOG = Logger.getLogger(ConnectorNodeMetadata.class.getName());

    public ConnectorNodeMetadata(MetadataResolver metadataResolver, String entityId) throws ComponentInitializationException {
        PredicateRoleDescriptorResolver predicateRoleDescriptorResolver = new PredicateRoleDescriptorResolver(metadataResolver);
        predicateRoleDescriptorResolver.initialize();
        metadataCredentialResolver = new MetadataCredentialResolver();
        metadataCredentialResolver.setRoleDescriptorResolver(predicateRoleDescriptorResolver);
        metadataCredentialResolver.setKeyInfoCredentialResolver(DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
        metadataCredentialResolver.initialize();
        this.entityId = entityId;
    }

    public PublicKey getEncryptionCertificate() throws ResolverException {
        CriteriaSet criteria = new CriteriaSet();
        criteria.add(new EntityIdCriterion(entityId));
        criteria.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteria.add(new UsageCriterion(UsageType.ENCRYPTION));

        try {
            return metadataCredentialResolver.resolveSingle(criteria).getPublicKey();
        } catch (ResolverException ex) {
            LOG.severe("Unable to read encryption certificate from metadata");
            throw ex;
        }
    }
}
