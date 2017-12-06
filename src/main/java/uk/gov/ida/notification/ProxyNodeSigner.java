package uk.gov.ida.notification;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;
import uk.gov.ida.notification.saml.XmlObjectMarshaller;

public class ProxyNodeSigner {

    private XmlObjectMarshaller marshaller;

    public ProxyNodeSigner(XmlObjectMarshaller marshaller) {

        this.marshaller = marshaller;
    }

    public AuthnRequest sign(AuthnRequest authRequest, Credential credential) throws Throwable{
        Signature signature = buildSignature(credential);
        authRequest.setSignature(signature);
        marshaller.marshall(authRequest);
        Signer.signObject(signature);
        return authRequest;
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
