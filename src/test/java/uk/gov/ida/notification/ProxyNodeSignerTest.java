package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.ida.notification.helpers.PKIHelpers.buildCredential;

public class ProxyNodeSignerTest {

    @Test
    public void shouldBuildProxyNodeSignature () throws Exception {
        InitializationService.initialize();
        String requestId = "requestId";
        AuthnRequestBuilder authnRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authRequest = authnRequestBuilder.buildObject();
        authRequest.setID(requestId);
        Credential credential = buildCredential(
                "local/hub_signing_primary.crt",
                "local/hub_signing_primary.pk8");

        ProxyNodeSigner proxyNodeSigner = new ProxyNodeSigner();

        AuthnRequest signedAuthnRequest = proxyNodeSigner.sign(authRequest, credential);

        assertEquals(requestId, signedAuthnRequest.getID());
        Signature signature = signedAuthnRequest.getSignature();
        assertNotNull(signature);
        String algoIdSignatureRsaSha256 = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        assertEquals(signature.getSignatureAlgorithm(), algoIdSignatureRsaSha256);
        assertEquals(signature.getSigningCredential(), credential);
        assertEquals(signature.getCanonicalizationAlgorithm(), SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        SignatureValidator.validate(signature, credential);
    }

}
