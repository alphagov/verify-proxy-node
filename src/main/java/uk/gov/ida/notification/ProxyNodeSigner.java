package uk.gov.ida.notification;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;

public class ProxyNodeSigner {

    public AuthnRequest sign(AuthnRequest authRequest, Credential credential) throws MarshallingException, SignatureException {
        Signature signature = buildSignature(credential);
        authRequest.setSignature(signature);
        marshallRequest(authRequest);
        Signer.signObject(signature);
        return authRequest;
    }

    private void marshallRequest(AuthnRequest authRequest) throws MarshallingException {
        MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(authRequest);
        marshaller.marshall(authRequest);
    }

    private Signature buildSignature(Credential credential) {
        String signatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        Signature signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setSigningCredential(credential);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        return signature;
    }
}
