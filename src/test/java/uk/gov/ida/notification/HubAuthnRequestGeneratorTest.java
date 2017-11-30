package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HubAuthnRequestGeneratorTest {
    @Test
    public void shouldGenerateHubAuthnRequestGivenEidas (){
        EidasAuthnRequest eidasRequest = mock(EidasAuthnRequest.class);
        AuthnRequest hubAuthnRequestUnsigned = mock(AuthnRequest.class);
        AuthnRequest hubAuthnRequestSigned = mock(AuthnRequest.class);
        EidasAuthnRequestTranslator translator = mock(EidasAuthnRequestTranslator.class);
        ProxyNodeSigner proxyNodeSigner = mock(ProxyNodeSigner.class);
        HubAuthnRequestGenerator hubAuthnRequestGenerator = new HubAuthnRequestGenerator(translator, proxyNodeSigner);
        when(translator.translate(eidasRequest)).thenReturn(hubAuthnRequestUnsigned);
        when(proxyNodeSigner.sign(hubAuthnRequestUnsigned)).thenReturn(hubAuthnRequestSigned);

        AuthnRequest hubAuthnRequest = hubAuthnRequestGenerator.generate(eidasRequest);

        assertEquals(hubAuthnRequest, hubAuthnRequestSigned);
    }
}
