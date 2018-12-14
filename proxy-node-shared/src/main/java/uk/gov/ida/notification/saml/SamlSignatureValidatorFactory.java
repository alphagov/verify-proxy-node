package uk.gov.ida.notification.saml;

import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

public class SamlSignatureValidatorFactory {
    public static SamlMessageSignatureValidator createSamlMessageSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) {
        ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = new ExplicitKeySignatureTrustEngine(
                hubMetadataResolverBundle.getMetadataCredentialResolver(),
                hubMetadataResolverBundle.getMetadataCredentialResolver().getKeyInfoCredentialResolver());
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
        return new SamlMessageSignatureValidator(metadataBackedSignatureValidator);
    }

    public static SamlRequestSignatureValidator createSamlRequestSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) {
        SamlMessageSignatureValidator samlMessageSignatureValidator = createSamlMessageSignatureValidator(hubMetadataResolverBundle);
        return new SamlRequestSignatureValidator(samlMessageSignatureValidator);
    }
}
