package uk.gov.ida.notification.saml.metadata;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import uk.gov.ida.notification.exceptions.metadata.ConnectorMetadataException;
import uk.gov.ida.notification.exceptions.metadata.InvalidMetadataException;
import uk.gov.ida.notification.exceptions.metadata.MissingMetadataException;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Metadata {
    private final MetadataCredentialResolver metadataCredentialResolver;

    public Metadata(MetadataCredentialResolver metadataCredentialResolver) {
        this.metadataCredentialResolver = metadataCredentialResolver;
    }

    public Credential getCredential(UsageType usageType, String entityId, QName descriptorQname) throws InvalidMetadataException, MissingMetadataException {
        CriteriaSet criteria = new CriteriaSet();
        criteria.add(new EntityIdCriterion(entityId));
        criteria.add(new EntityRoleCriterion(descriptorQname));
        criteria.add(new UsageCriterion(usageType));

        try {
            Credential credential = metadataCredentialResolver.resolveSingle(criteria);
            if (credential == null) {
                throw new InvalidMetadataException(String.format("Missing %s certificate from Connector Metadata with entityID %s", usageType, entityId));
            }
            return credential;
        } catch (ResolverException ex) {
            throw new MissingMetadataException(String.format("Unable to resolve metadata credentials from Connector Metadata with entityID %s", entityId), ex);
        }
    }

    public Endpoint getEndpoint(String entityId, QName role) throws ResolverException {
        CriteriaSet criteria = new CriteriaSet();
        criteria.add(new EntityIdCriterion(entityId));
        criteria.add(new EntityRoleCriterion(role));

        final RoleDescriptor roleDescriptor = Optional.ofNullable(metadataCredentialResolver.getRoleDescriptorResolver())
                .orElseThrow()
                .resolveSingle(criteria);

        return Optional.ofNullable(roleDescriptor)
                .map(RoleDescriptor::getEndpoints)
                .orElseThrow()
                .stream()
                .filter(sso -> sso.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI))
                .findFirst()
                .orElseThrow();
    }

    public static String getAssertionConsumerServiceLocation(String entityId, MetadataResolver metadataResolver) {

        final List<String> locationsFromMetadata =
                getAssertionConsumerServicesFromMetadata(
                        entityId,
                        metadataResolver
                ).stream()
                        .filter(assertionConsumerService -> SAMLConstants.SAML2_POST_BINDING_URI.equals(assertionConsumerService.getBinding()))
                        .map(AssertionConsumerService::getLocation)
                        .collect(Collectors.toList());

        if (locationsFromMetadata.size() == 0) {
            throw new ConnectorMetadataException("Unable to load 'Location' for 'AssertionConsumerService' from Connector Metadata for entityId: " + entityId);
        } else {
            return locationsFromMetadata.get(0);
        }
    }

    private static Collection<AssertionConsumerService> getAssertionConsumerServicesFromMetadata(String entityId, MetadataResolver metadataResolver) {
        try {
            EntityIdCriterion entityIdCriterion = new EntityIdCriterion(entityId);
            EntityDescriptor metadata = metadataResolver.resolveSingle(new CriteriaSet(entityIdCriterion));

            if (metadata == null) {
                return List.of();
            }

            SPSSODescriptor spssoDescriptor = metadata.getSPSSODescriptor(SAMLConstants.SAML20P_NS);

            if (spssoDescriptor == null || spssoDescriptor.getAssertionConsumerServices() == null) {
                return List.of();
            }

            return spssoDescriptor.getAssertionConsumerServices();
        } catch (ResolverException e) {
            return List.of();
        }
    }
}
