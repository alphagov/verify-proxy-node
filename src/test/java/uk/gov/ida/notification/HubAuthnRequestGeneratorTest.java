package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HubAuthnRequestGeneratorTest extends SamlInitializedTest {

    @Test
    public void shouldTranslateAndSignEidasAuthnRequests() {
        EidasAuthnRequestTranslator translator = mock(EidasAuthnRequestTranslator.class);
        SamlObjectSigner samlObjectSigner = mock(SamlObjectSigner.class);

        EidasAuthnRequest eidasAuthnRequest = mock(EidasAuthnRequest.class);
        AuthnRequest translatedAuthnRequest = mock(AuthnRequest.class);
        AuthnRequest expectedHubAuthnRequest = mock(AuthnRequest.class);

        when(translator.translate(eidasAuthnRequest)).thenReturn(translatedAuthnRequest);
        when(samlObjectSigner.sign(translatedAuthnRequest)).thenReturn(expectedHubAuthnRequest);

        HubAuthnRequestGenerator hubAuthnRequestGenerator = new HubAuthnRequestGenerator(translator, samlObjectSigner);
        AuthnRequest actualHubAuthnRequest = hubAuthnRequestGenerator.generate(eidasAuthnRequest);

        assertEquals(expectedHubAuthnRequest, actualHubAuthnRequest);
    }
}
