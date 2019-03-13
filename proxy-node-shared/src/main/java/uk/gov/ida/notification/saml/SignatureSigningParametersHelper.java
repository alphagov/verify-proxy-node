package uk.gov.ida.notification.saml;

import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

public class SignatureSigningParametersHelper {
    private static final X509KeyInfoGeneratorFactory keyInfoGeneratorFactory;

    static {
        keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
    }

    public static SignatureSigningParameters build(BasicX509Credential credential, String algorithm) {
        SignatureSigningParameters signingParams = new SignatureSigningParameters();
        signingParams.setSignatureAlgorithm(algorithm);
        signingParams.setSignatureCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signingParams.setSigningCredential(credential);
        signingParams.setKeyInfoGenerator(keyInfoGeneratorFactory.newInstance());
        return signingParams;
    }
}
