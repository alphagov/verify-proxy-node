package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.common.SAMLObject;
import uk.gov.ida.notification.saml.XmlObjectMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

public class SamlFormViewMapper {
    private XmlObjectMarshaller marshaller;

    public SamlFormViewMapper(XmlObjectMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    public SamlFormView map(String url, String samlMessageType, SAMLObject samlObject, String submitTest) throws Throwable{
        String samlMessage = marshaller.transformToString(samlObject);
        String encodedSamlMessage = Base64.encodeAsString(samlMessage);
        return new SamlFormView(url, samlMessageType, encodedSamlMessage, submitTest);
    }
}
