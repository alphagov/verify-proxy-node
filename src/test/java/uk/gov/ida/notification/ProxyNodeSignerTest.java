package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ProxyNodeSignerTest {

    @Test
    public void shouldBuildProxyNodeSignature (){
        AuthnRequest unsignedAuthnRequest = mock(AuthnRequest.class);
        ProxyNodeSigner proxyNodeSigner = new ProxyNodeSigner();

        AuthnRequest signedAuthnRequest = proxyNodeSigner.sign(unsignedAuthnRequest);

        assertEquals(signedAuthnRequest, unsignedAuthnRequest);
    }
}
