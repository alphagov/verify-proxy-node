package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import uk.gov.ida.notification.exceptions.SamlSigningException;
import uk.gov.ida.notification.pki.SigningCredential;

import java.util.Base64;


public class SamlObjectSigner {
    private final SigningCredential signingCredential;
    private final SamlObjectMarshaller marshaller;

    public SamlObjectSigner(SigningCredential signingCredential) {
        this.signingCredential = signingCredential;
        this.marshaller = new SamlObjectMarshaller();
    }

    public <T extends SignableSAMLObject> T sign(T signableSAMLObject) {
        Signature signature = buildSignature();
        signableSAMLObject.setSignature(signature);
        try {
            marshaller.marshallToElement(signableSAMLObject);
            Signer.signObject(signature);
        } catch (MarshallingException | SignatureException e) {
            throw new SamlSigningException(e);
        }
        return signableSAMLObject;
    }

    private Signature buildSignature() {
        String signatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        Signature signature = SamlBuilder.build(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setSigningCredential(signingCredential);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setKeyInfo(buildKeyInfo());
        return signature;
    }

    private KeyInfo buildKeyInfo() {
        KeyInfo keyInfo = SamlBuilder.build(KeyInfo.DEFAULT_ELEMENT_NAME);
        X509Data x509Data = SamlBuilder.build(X509Data.DEFAULT_ELEMENT_NAME);
        X509Certificate x509Certificate = SamlBuilder.build(X509Certificate.DEFAULT_ELEMENT_NAME);

        x509Certificate.setValue(signingCredential.getCertificateString());
        x509Data.getX509Certificates().add(x509Certificate);
        keyInfo.getX509Datas().add(x509Data);
        return keyInfo;
    }
}
