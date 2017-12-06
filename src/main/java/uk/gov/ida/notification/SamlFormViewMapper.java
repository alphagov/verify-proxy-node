package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

import javax.xml.transform.TransformerException;

public class SamlFormViewMapper {
    private SamlMarshaller marshaller;

    public SamlFormViewMapper(SamlMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    public SamlFormView map(String url, String samlMessageType, AuthnRequest authnRequest, String submitTest) throws Throwable{
        String samlMessage = marshaller.samlObjectToString(authnRequest);
        String encodedSamlMessage = Base64.encodeAsString(samlMessage);
        return new SamlFormView(url, samlMessageType, encodedSamlMessage, submitTest);
    }
}
