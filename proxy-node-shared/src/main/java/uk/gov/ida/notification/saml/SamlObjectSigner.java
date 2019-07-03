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
import org.slf4j.MDC;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

public class SamlObjectSigner {

    private final SignatureSigningParameters signingParams;
    private final Long keyHandle;

    public SamlObjectSigner(BasicX509Credential signingCredential, String signingAlgorithm, Long keyHandle) {
        this.signingParams = SignatureSigningParametersHelper.build(signingCredential, signingAlgorithm);
        this.keyHandle = keyHandle;
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
        if ( this.keyHandle != null ) {
            ProxyNodeLogger.addContext(ProxyNodeMDCKey.HSM_KEY_HANDLE, this.keyHandle.toString());
        }
        ProxyNodeLogger.info("Sending a request to the HSM to sign eIDAS SAML message");
        MDC.remove(ProxyNodeMDCKey.SIGNING_PROVIDER.name());
        MDC.remove(ProxyNodeMDCKey.HSM_KEY_HANDLE.name());
    }
}
