package uk.gov.ida.notification.saml;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

public class SamlSignatureValidatorFactory {

    public static SamlRequestSignatureValidator<AuthnRequest> createSamlRequestSignatureValidator(MetadataResolverBundle metadataResolverBundle) {
        SamlMessageSignatureValidator samlMessageSignatureValidator = createSamlMessageSignatureValidator(metadataResolverBundle);
        return new SamlRequestSignatureValidator<>(samlMessageSignatureValidator);
    }

    private static SamlMessageSignatureValidator createSamlMessageSignatureValidator(MetadataResolverBundle metadataResolverBundle) {
        MetadataCredentialResolver metadataCredentialResolver = metadataResolverBundle.getMetadataCredentialResolver();
        KeyInfoCredentialResolver keyInfoCredentialResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
        ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = new ExplicitKeySignatureTrustEngine(metadataCredentialResolver, keyInfoCredentialResolver);
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
        return new SamlMessageSignatureValidator(metadataBackedSignatureValidator);
    }
}
