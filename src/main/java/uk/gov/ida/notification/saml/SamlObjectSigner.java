package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import uk.gov.ida.notification.pki.SigningCredential;

public class SamlObjectSigner {
    private final SigningCredential signingCredential;
    private final SamlObjectMarshaller marshaller;

    public SamlObjectSigner(SigningCredential signingCredential) {
        this.signingCredential = signingCredential;
        this.marshaller = new SamlObjectMarshaller();
    }

    public <T extends SignableSAMLObject> T sign(T signableSAMLObject) {
        Signature signature = buildSignature(signingCredential);
        signableSAMLObject.setSignature(signature);
        try {
            marshaller.marshallToElement(signableSAMLObject);
            Signer.signObject(signature);
        } catch (MarshallingException | SignatureException e) {
            throw new SamlSigningException(e);
        }
        return signableSAMLObject;
    }

    private Signature buildSignature(SigningCredential credential) {
        String signatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        Signature signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setSigningCredential(credential);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        return signature;
    }
}
