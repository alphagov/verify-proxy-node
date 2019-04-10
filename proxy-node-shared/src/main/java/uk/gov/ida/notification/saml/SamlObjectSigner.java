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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.notification.shared.ProxyNodeLogger;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

public class SamlObjectSigner {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final SignatureSigningParameters signingParams;
    private ProxyNodeLogger proxyNodeLogger;

    public SamlObjectSigner(BasicX509Credential signingCredential, String signingAlgorithm, ProxyNodeLogger proxyNodeLogger) {
        this.proxyNodeLogger = proxyNodeLogger;
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
        proxyNodeLogger.addContext(ProxyNodeMDCKey.eidasRequestId, responseId);
        proxyNodeLogger.addContext(ProxyNodeMDCKey.signingProvider, signingProvider);
        log.info("Signing eIDAS response");
    }
}
