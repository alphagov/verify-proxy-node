package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.ResponseAssertionEncrypter;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.translation.HubResponseContainer;
import uk.gov.ida.notification.saml.translation.HubResponseTranslator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EidasResponseGeneratorTest {
    @Test
    public void shouldTranslateAndSignAndEncryptHubResponses() throws Exception {
        HubResponseContainer hubResponseContainer = mock(HubResponseContainer.class);
        Response translatedResponse = mock(Response.class);
        Response encryptedResponse = mock(Response.class);
        Response expectedSignedResponse = mock(Response.class);

        HubResponseTranslator hubResponseTranslator = mock(HubResponseTranslator.class);
        ResponseAssertionEncrypter responseAssertionEncrypter = mock(ResponseAssertionEncrypter.class);
        SamlObjectSigner signer = mock(SamlObjectSigner.class);

        when(hubResponseTranslator.translate(hubResponseContainer)).thenReturn(translatedResponse);
        when(responseAssertionEncrypter.encrypt(translatedResponse)).thenReturn(encryptedResponse);
        when(signer.sign(encryptedResponse)).thenReturn(expectedSignedResponse);

        EidasResponseGenerator eidasResponseGenerator = new EidasResponseGenerator(hubResponseTranslator, signer);
        Response actualResponse = eidasResponseGenerator.generate(hubResponseContainer, responseAssertionEncrypter);

        assertEquals(expectedSignedResponse, actualResponse);
    }
}