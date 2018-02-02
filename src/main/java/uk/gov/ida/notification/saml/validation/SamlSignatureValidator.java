package uk.gov.ida.notification.saml.validation;

import net.shibboleth.utilities.java.support.resolver.Criterion;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;

public class SamlSignatureValidator {
    public void validateResponse(Credential credential, Response response) throws MarshallingException {
        SignatureValidator signatureValidator = new SignatureValidator() {
            @Override
            protected TrustEngine<Signature> getTrustEngine(String entityId) {
                CredentialResolver credentialResolver = new StaticCredentialResolver(credential);
                KeyInfoCredentialResolver kiResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
                return new ExplicitKeySignatureTrustEngine(credentialResolver, kiResolver);
            }

            @Override
            protected List<Criterion> getAdditionalCriteria(String entityId, QName role) {
                return Arrays.asList(
                        new EntityIdCriterion(entityId),
                        new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME),
                        new UsageCriterion(UsageType.SIGNING)
                );
            }
        };
        SamlMessageSignatureValidator samlMessageSignatureValidator = new SamlMessageSignatureValidator(signatureValidator);
        SamlResponseSignatureValidator samlResponseSignatureValidator = new SamlResponseSignatureValidator(samlMessageSignatureValidator);

        samlResponseSignatureValidator.validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }
}
