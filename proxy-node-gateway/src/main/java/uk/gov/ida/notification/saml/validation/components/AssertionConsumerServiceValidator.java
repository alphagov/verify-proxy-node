package uk.gov.ida.notification.saml.validation.components;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

public class AssertionConsumerServiceValidator {
    private static final Logger LOG = Logger.getLogger(AssertionConsumerServiceValidator.class.getName());

    private final MetadataResolver metadataResolver;

    public AssertionConsumerServiceValidator(MetadataResolver metadataResolver) {
        this.metadataResolver = metadataResolver;
    }

    public void validate(AuthnRequest request) {
        String supplied = request.getAssertionConsumerServiceURL();
        if (Strings.isNullOrEmpty(supplied)) return;

        String entityId = request.getIssuer().getValue();
        Collection<String> inMetadata = getAssertionConsumerServicesFromMetadata(entityId).stream()
        .map(AssertionConsumerService::getLocation)
        .collect(Collectors.toList());

        for (String location : inMetadata) {
            if (location.equals(supplied)) return;
        }

        throw new InvalidAuthnRequestException("Supplied AssertionConsumerServiceURL has no match in metadata. " +
        String.format("Supplied: %s. In metadata: %s.", supplied, String.join(", ", inMetadata)));
    }

    private Collection<AssertionConsumerService> getAssertionConsumerServicesFromMetadata(String entityId) {
        try {
            EntityIdCriterion criterion = new EntityIdCriterion(entityId);
            EntityDescriptor metadata = metadataResolver.resolveSingle(new CriteriaSet(criterion));
            if (metadata == null) return List.of();

            SPSSODescriptor service = metadata.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
            if (service == null) return List.of();

            return service.getAssertionConsumerServices();
        } catch (ResolverException e) {
            LOG.warning("Unable to resolve metadata for entity " + entityId);
            return List.of();
        }
    }
}