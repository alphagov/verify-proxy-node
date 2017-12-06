package uk.gov.ida.notification;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamlFormViewMapperTest {

    @Test
    public void shouldMapAuthnRequestToSamlFormView() throws Throwable {
        String submitTest = "submit text";
        String url = "url";
        String samlMessageType = "saml message type";
        String authnRequestAsString = "authnRequest as string";
        AuthnRequest authnRequest = mock(AuthnRequest.class);
        SamlMarshaller marshaller = mock(SamlMarshaller.class);
        SamlFormViewMapper viewMapper = new SamlFormViewMapper(marshaller);
        when(marshaller.samlObjectToString(authnRequest)).thenReturn(authnRequestAsString);

        SamlFormView view = viewMapper.map(url, samlMessageType, authnRequest, submitTest);

        assertEquals(view.getSubmitText(), submitTest);
        assertEquals(view.getPostUrl(), url);
        assertEquals(view.getEncodedSamlMessage(), Base64.encodeAsString(authnRequestAsString));
        assertEquals(view.getSamlMessageType(), samlMessageType);
    }
}
