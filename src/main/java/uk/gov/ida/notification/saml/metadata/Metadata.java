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
import org.opensaml.security.x509.X509Credential;
import uk.gov.ida.notification.exceptions.metadata.InvalidMetadataException;
import uk.gov.ida.notification.exceptions.metadata.MissingMetadataException;

import java.security.PublicKey;

public class Metadata {
    private final MetadataCredentialResolver metadataCredentialResolver;

    public Metadata(MetadataCredentialResolver metadataCredentialResolver) {
        this.metadataCredentialResolver = metadataCredentialResolver;
    }

    public X509Credential getEncryptionCredential(String entityId) throws ResolverException, MissingMetadataException {
        CriteriaSet criteria = new CriteriaSet();
        criteria.add(new EntityIdCriterion(entityId));
        criteria.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteria.add(new UsageCriterion(UsageType.ENCRYPTION));

        try {
            X509Credential encryptionCredential = (X509Credential) metadataCredentialResolver.resolveSingle(criteria);
            if (encryptionCredential == null) throw new MissingMetadataException("Missing Encryption certificate");
            return encryptionCredential;
        } catch(ResolverException ex) {
            throw new InvalidMetadataException("Unable to resolve metadata credentials", ex);
        }
    }

    public PublicKey getSigningPublicKey(String entityId) throws ResolverException, MissingMetadataException {
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
