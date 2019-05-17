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
import uk.gov.ida.notification.shared.ProxyNodeLogger;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

public class SamlObjectSigner {

    private final SignatureSigningParameters signingParams;

    public SamlObjectSigner(BasicX509Credential signingCredential, String signingAlgorithm) {
        signingParams = SignatureSigningParametersHelper.build(signingCredential, signingAlgorithm);
    }

    public void sign(SignableSAMLObject signableSAMLObject, String responseId) throws MarshallingException, SecurityException, SignatureException {
        logSigningRequest(responseId, signingParams.getSigningCredential().getEntityId());
        SignatureSupport.signObject(signableSAMLObject, signingParams);
        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
        signatureProfileValidator.validate(signableSAMLObject.getSignature());
        SignatureValidator.validate(signableSAMLObject.getSignature(), signingParams.getSigningCredential());
    }

    public SignatureSigningParameters getSigningParams() {
        return signingParams;
    }

    private  void logSigningRequest(String responseId, String signingProvider) {
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EIDAS_REQUEST_ID, responseId);
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.SIGNING_PROVIDER, signingProvider);
        ProxyNodeLogger.info("Sending a request to the HSM to sign eIDAS SAML message");
    }
}
