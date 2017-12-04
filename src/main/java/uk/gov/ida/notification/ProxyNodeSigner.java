package uk.gov.ida.notification;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

public class ProxyNodeSigner {

    public AuthnRequest sign(AuthnRequest authRequest) {
        String signatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        Signature signature = new SignatureBuilder()
                .buildObject();
        signature.setSignatureAlgorithm(signatureAlgorithm);
        authRequest.setSignature(signature);
        return authRequest;
    }
}
