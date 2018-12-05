package uk.gov.ida.notification.saml;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.notification.saml.metadata.MetadataCredentialResolverInitializer;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

public class SamlSignatureValidatorFactory {


    public static SamlMessageSignatureValidator createSamlMessageSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) throws ComponentInitializationException {
        MetadataCredentialResolver hubMetadataCredentialResolver = new MetadataCredentialResolverInitializer(hubMetadataResolverBundle.getMetadataResolver()).initialize();
        KeyInfoCredentialResolver keyInfoCredentialResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();
        ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine = new ExplicitKeySignatureTrustEngine(hubMetadataCredentialResolver, keyInfoCredentialResolver);
        MetadataBackedSignatureValidator metadataBackedSignatureValidator = MetadataBackedSignatureValidator.withoutCertificateChainValidation(explicitKeySignatureTrustEngine);
        return new SamlMessageSignatureValidator(metadataBackedSignatureValidator);
    }

    public static SamlRequestSignatureValidator createSamlRequestSignatureValidator(MetadataResolverBundle hubMetadataResolverBundle) throws ComponentInitializationException {
        SamlMessageSignatureValidator samlMessageSignatureValidator = createSamlMessageSignatureValidator(hubMetadataResolverBundle);
        return new SamlRequestSignatureValidator(samlMessageSignatureValidator);
    }
}
