package uk.gov.ida.notification.saml.metadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.RoleDescriptorResolver;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import uk.gov.ida.notification.exceptions.metadata.InvalidMetadataException;
import uk.gov.ida.notification.exceptions.metadata.MissingMetadataException;

import javax.xml.namespace.QName;

public class Metadata {
    private final MetadataCredentialResolver metadataCredentialResolver;

    public Metadata(MetadataCredentialResolver metadataCredentialResolver) {
        this.metadataCredentialResolver = metadataCredentialResolver;
    }

    public Credential getCredential(UsageType usageType, String entityId, QName descriptorQname) throws MissingMetadataException {
        CriteriaSet criteria = new CriteriaSet();
        criteria.add(new EntityIdCriterion(entityId));
        criteria.add(new EntityRoleCriterion(descriptorQname));
        criteria.add(new UsageCriterion(usageType));

        try {
            Credential credential = metadataCredentialResolver.resolveSingle(criteria);
            if (credential == null) throw new MissingMetadataException(String.format("Missing %s certificate", usageType));
            return credential;
        } catch(ResolverException ex) {
            throw new InvalidMetadataException("Unable to resolve metadata credentials", ex);
        }
    }

    public Endpoint getEndpoint(String entityId, QName role) throws ResolverException {
        CriteriaSet criteria = new CriteriaSet();
        criteria.add(new EntityIdCriterion(entityId));
        criteria.add(new EntityRoleCriterion(role));

        RoleDescriptorResolver roleDescriptorResolver = metadataCredentialResolver.getRoleDescriptorResolver();

        return roleDescriptorResolver.resolveSingle(criteria).getEndpoints().stream()
            .filter(sso -> sso.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI))
            .findFirst()
            .orElseThrow();
    }

    public RoleDescriptorResolver getRoleDescriptorResolver() {
        return metadataCredentialResolver.getRoleDescriptorResolver();
    }
}
