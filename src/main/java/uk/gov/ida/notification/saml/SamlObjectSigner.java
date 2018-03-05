package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import uk.gov.ida.notification.exceptions.saml.SamlSigningException;

import java.security.PrivateKey;
import java.security.PublicKey;

public class SamlObjectSigner {
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";

    private final SamlObjectMarshaller marshaller;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final String certificate;

    public SamlObjectSigner(PublicKey publicKey, PrivateKey privateKey, String certificate) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.certificate = certificate;
        this.marshaller = new SamlObjectMarshaller();
    }

    public void sign(SignableSAMLObject signableSAMLObject) {
        Signature signature = buildSignature();
        signableSAMLObject.setSignature(signature);
        try {
            marshaller.marshallToElement(signableSAMLObject);
            Signer.signObject(signature);
        } catch (MarshallingException | SignatureException e) {
            throw new SamlSigningException(e);
        }
    }

    private Signature buildSignature() {
        String signatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        Signature signature = SamlBuilder.build(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setSigningCredential(new BasicCredential(publicKey, privateKey));
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setKeyInfo(buildKeyInfo());
        return signature;
    }

    private KeyInfo buildKeyInfo() {
        KeyInfo keyInfo = SamlBuilder.build(KeyInfo.DEFAULT_ELEMENT_NAME);
        X509Data x509Data = SamlBuilder.build(X509Data.DEFAULT_ELEMENT_NAME);
        X509Certificate x509Certificate = SamlBuilder.build(X509Certificate.DEFAULT_ELEMENT_NAME);

        x509Certificate.setValue(certificate.replace(BEGIN_CERT, "").replace(END_CERT, ""));
        x509Data.getX509Certificates().add(x509Certificate);
        keyInfo.getX509Datas().add(x509Data);
        return keyInfo;
    }
}
