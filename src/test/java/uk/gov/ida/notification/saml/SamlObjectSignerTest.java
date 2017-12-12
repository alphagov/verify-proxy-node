package uk.gov.ida.notification.saml;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.pki.SigningCredential;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SamlObjectSignerTest {
    private TestKeyPair testKeyPair;

    @Before
    public void before() throws Throwable {
        InitializationService.initialize();
        testKeyPair = new TestKeyPair();
    }

    @Test
    public void shouldSignAuthRequest() throws Throwable {
        SigningCredential credential = new SigningCredential(testKeyPair.getPublicKey(), testKeyPair.getPrivateKey());
        SamlObjectSigner samlObjectSigner = new SamlObjectSigner(credential);
        AuthnRequestBuilder authnRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authnRequest = authnRequestBuilder.buildObject();
        AuthnRequest signedAuthnRequest = samlObjectSigner.sign(authnRequest);
        Signature signature = signedAuthnRequest.getSignature();

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
