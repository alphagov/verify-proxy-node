package uk.gov.ida.notification.saml;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.pki.SigningCredential;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SamlObjectSignerTest extends SamlInitializedTest {
    private TestKeyPair testKeyPair;

    @Before
    public void setup() throws Throwable {
        testKeyPair = new TestKeyPair();
    }

    @Test
    public void shouldSignAuthRequest() throws Throwable {
        SigningCredential credential = new SigningCredential(testKeyPair.publicKey, testKeyPair.privateKey, testKeyPair.getEncodedCertificate());
        SamlObjectSigner samlObjectSigner = new SamlObjectSigner(credential);
        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);
        samlObjectSigner.sign(authnRequest);
        Signature signature = authnRequest.getSignature();

        String actualCertificate = signature.getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue();

        assertEquals(testKeyPair.getEncodedCertificate(), actualCertificate);
        signatureShouldBeValid(credential, signature);
    }

    private void signatureShouldBeValid(Credential credential, Signature signature) throws SignatureException {
        assertNotNull(signature);
        String algoIdSignatureRsaSha256 = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        assertEquals(signature.getSignatureAlgorithm(), algoIdSignatureRsaSha256);
        assertEquals(signature.getSigningCredential(), credential);
        assertEquals(signature.getCanonicalizationAlgorithm(), SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        SignatureValidator.validate(signature, credential);
    }
}
