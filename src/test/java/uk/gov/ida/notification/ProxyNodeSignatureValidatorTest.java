package uk.gov.ida.notification;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;
import uk.gov.ida.notification.helpers.PKIHelpers;
import uk.gov.ida.notification.helpers.SamlMarshallerHelper;

public class ProxyNodeSignatureValidatorTest {
    private ProxyNodeSignatureValidator proxyNodeSignatureValidator = new ProxyNodeSignatureValidator();
    private SamlMarshallerHelper marshallerHelper = new SamlMarshallerHelper();

    @BeforeClass
    public static void beforeClass() throws InitializationException {
        InitializationService.initialize();
    }

    @Test
    public void shouldNotThrowWhenValidSignature() throws Throwable {
        Credential credential = PKIHelpers.buildAnyCredential();
        Response response = buildSignedResponse(credential);

        proxyNodeSignatureValidator.validate(response, credential);
    }

    @Test(expected = Throwable.class)
    public void shouldThrowWhenSignedWithDifferentCredentials() throws Throwable {
        Credential credential = PKIHelpers.buildAnyCredential();
        Credential otherCredential = PKIHelpers.buildHubEncryptionCredential();
        Response response = buildSignedResponse(otherCredential);

        proxyNodeSignatureValidator.validate(response, credential);
    }

    @Test(expected = Throwable.class)
    public void shouldBeFalseWhenNoSignature() throws Throwable {
        Credential credential = PKIHelpers.buildAnyCredential();
        Response response = (Response)XMLObjectSupport.buildXMLObject(Response.DEFAULT_ELEMENT_NAME);

        proxyNodeSignatureValidator.validate(response, credential);
    }

    private Response buildSignedResponse(Credential credential) throws Throwable {
        Response response = (Response) XMLObjectSupport.buildXMLObject(Response.DEFAULT_ELEMENT_NAME);
        String signatureAlgorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        Signature signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setSigningCredential(credential);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        response.setSignature(signature);
        marshallerHelper.mashall(response);
        Signer.signObject(signature);
        return response;
    }
}
