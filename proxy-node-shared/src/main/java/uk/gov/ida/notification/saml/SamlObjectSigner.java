package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureSupport;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import uk.gov.ida.notification.logging.TranslatorSigningLoggerHelper;

public class SamlObjectSigner {
    private final SignatureSigningParameters signingParams;
    
    public SamlObjectSigner(BasicX509Credential credential) {
        this(credential, SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
    }

    public SamlObjectSigner(BasicX509Credential signingCredential, String signingAlgorithm) {
        X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        signingParams = new SignatureSigningParameters();
        signingParams.setSignatureAlgorithm(signingAlgorithm);
        signingParams.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signingParams.setSigningCredential(signingCredential);
        signingParams.setKeyInfoGenerator(keyInfoGeneratorFactory.newInstance());
    }

    public void sign(SignableSAMLObject signableSAMLObject, String responseId) throws MarshallingException, SecurityException, SignatureException {
        TranslatorSigningLoggerHelper.logSigningRequest(responseId, signingParams.getSigningCredential().getEntityId());
        SignatureSupport.signObject(signableSAMLObject, signingParams);
        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
        signatureProfileValidator.validate(signableSAMLObject.getSignature());
        SignatureValidator.validate(signableSAMLObject.getSignature(), signingParams.getSigningCredential());
    }

    public Credential getCredential() {
        return signingParams.getSigningCredential();
    }
}
