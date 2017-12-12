package uk.gov.ida.notification;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Test;
import org.opensaml.saml.common.SAMLObject;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamlFormViewMapperTest {

    @Test
    public void shouldMapSamlObjectToSamlFormView() throws Throwable {
        String submitTest = "submit text";
        String url = "url";
        String samlMessageType = "saml message type";
        String authnRequestAsString = "authnRequest as string";
        SAMLObject samlObject = mock(SAMLObject.class);
        SamlObjectMarshaller marshaller = mock(SamlObjectMarshaller.class);
        SamlFormViewMapper viewMapper = new SamlFormViewMapper(marshaller);
        when(marshaller.transformToString(samlObject)).thenReturn(authnRequestAsString);

        SamlFormView view = viewMapper.map(url, samlMessageType, samlObject, submitTest);

        assertEquals(view.getSubmitText(), submitTest);
        assertEquals(view.getPostUrl(), url);
        assertEquals(view.getEncodedSamlMessage(), Base64.encodeAsString(authnRequestAsString));
        assertEquals(view.getSamlMessageType(), samlMessageType);
    }
}
