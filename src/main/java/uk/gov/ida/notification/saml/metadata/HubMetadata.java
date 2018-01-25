package uk.gov.ida.notification.saml.metadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import uk.gov.ida.notification.exceptions.MissingMetadataException;

import java.security.PublicKey;

public class HubMetadata {
    private final MetadataCredentialResolver metadataCredentialResolver;
    private final String entityId;

    public HubMetadata(MetadataCredentialResolver metadataCredentialResolver, String entityId) {
        this.metadataCredentialResolver = metadataCredentialResolver;
        this.entityId = entityId;
    }

    public PublicKey getSigningPublicKey() throws ResolverException, MissingMetadataException {
        CriteriaSet criteria = new CriteriaSet();
        criteria.add(new EntityIdCriterion(entityId));
        criteria.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteria.add(new UsageCriterion(UsageType.SIGNING));

        try {
            Credential signingCredential = metadataCredentialResolver.resolveSingle(criteria);
            if (signingCredential == null) throw new MissingMetadataException("Missing Signing key");
            return signingCredential.getPublicKey();
        } catch(ResolverException ex) {
            throw new ResolverException("Unable to resolve metadata credentials", ex);
        }
    }
}
