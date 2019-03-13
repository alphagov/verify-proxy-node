package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureSupport;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

public class SamlObjectSigner {
    private final SignatureSigningParameters signingParams;

    public SamlObjectSigner(BasicX509Credential signingCredential, String signingAlgorithm) {
        signingParams = ParametersFactory.buildSignatureSigningParameters(signingCredential, signingAlgorithm);
    }

    public void sign(SignableSAMLObject signableSAMLObject) throws MarshallingException, SecurityException, SignatureException {
        SignatureSupport.signObject(signableSAMLObject, signingParams);
        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
        signatureProfileValidator.validate(signableSAMLObject.getSignature());
        SignatureValidator.validate(signableSAMLObject.getSignature(), signingParams.getSigningCredential());
    }

    public SignatureSigningParameters getSigningParams() {
        return signingParams;
    }
}