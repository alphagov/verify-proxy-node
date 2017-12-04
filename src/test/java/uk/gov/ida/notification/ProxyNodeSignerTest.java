package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProxyNodeSignerTest {

    @Test
    public void shouldBuildProxyNodeSignature () throws InitializationException {
        String requestId = "requestId";
        AuthnRequestBuilder authnRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authRequest = authnRequestBuilder.buildObject();
        authRequest.setID(requestId);
        ProxyNodeSigner proxyNodeSigner = new ProxyNodeSigner();

        AuthnRequest signedAuthnRequest = proxyNodeSigner.sign(authRequest);

        assertEquals(requestId, signedAuthnRequest.getID());
        assertNotNull(signedAuthnRequest.getSignature());
        assertEquals(signedAuthnRequest.getSignature().getSignatureAlgorithm(), SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
    }
}
