package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HubAuthnRequestGeneratorTest {
    @Test
    public void shouldGenerateHubAuthnRequestGivenEidas () throws Throwable {
        CredentialRepository credentialRepository = mock(CredentialRepository.class);
        Credential credential = mock(Credential.class);
        EidasAuthnRequest eidasRequest = mock(EidasAuthnRequest.class);
        AuthnRequest hubAuthnRequestUnsigned = mock(AuthnRequest.class);
        AuthnRequest hubAuthnRequestSigned = mock(AuthnRequest.class);
        EidasAuthnRequestTranslator translator = mock(EidasAuthnRequestTranslator.class);
        ProxyNodeSigner proxyNodeSigner = mock(ProxyNodeSigner.class);
        HubAuthnRequestGenerator hubAuthnRequestGenerator = new HubAuthnRequestGenerator
                (translator, proxyNodeSigner, credentialRepository);
        when(translator.translate(eidasRequest)).thenReturn(hubAuthnRequestUnsigned);
        when(credentialRepository.getHubCredential()).thenReturn(credential);
        when(proxyNodeSigner.sign(hubAuthnRequestUnsigned, credential)).thenReturn(hubAuthnRequestSigned);

        AuthnRequest hubAuthnRequest = hubAuthnRequestGenerator.generate(eidasRequest);

        assertEquals(hubAuthnRequest, hubAuthnRequestSigned);
    }
}
