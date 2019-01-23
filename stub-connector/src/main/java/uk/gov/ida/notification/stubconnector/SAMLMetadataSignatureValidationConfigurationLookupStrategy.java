package uk.gov.ida.notification.stubconnector;

import com.google.common.base.Function;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.metadata.criteria.role.impl.RoleDescriptorCriterionPredicateRegistry;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.xmlsec.SignatureValidationConfiguration;
import org.opensaml.xmlsec.SignatureValidationParameters;
import org.opensaml.xmlsec.SignatureValidationParametersResolver;
import org.opensaml.xmlsec.criterion.SignatureValidationConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureValidationParametersResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SAMLMetadataSignatureValidationConfigurationLookupStrategy implements Function<MessageContext, List<SignatureValidationConfiguration>> {
    /** Logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(SAMLMetadataSignatureValidationConfigurationLookupStrategy.class);

    @Nullable
    @Override
    public List<SignatureValidationConfiguration> apply(@Nullable MessageContext messageContext) {
        SAMLMetadataContext metadataContext = messageContext.getSubcontext(SAMLMetadataContext.class);
        RoleDescriptor roleDescriptor = metadataContext.getRoleDescriptor();
        RoleDescriptorCriterion roleDescriptorCriterion = new RoleDescriptorCriterion(roleDescriptor);
        CriteriaSet criteria = new CriteriaSet();
        criteria.add(roleDescriptorCriterion);

        BasicSignatureValidationParametersResolver parametersResolver = new BasicSignatureValidationParametersResolver();
        try {
            parametersResolver.resolve(criteria);
        } catch (ResolverException e) {
            e.printStackTrace();
        }

        return null;
    }
}
