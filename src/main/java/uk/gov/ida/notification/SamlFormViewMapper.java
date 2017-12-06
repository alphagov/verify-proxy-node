package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.XmlObjectMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

public class SamlFormViewMapper {
    private XmlObjectMarshaller marshaller;

    public SamlFormViewMapper(XmlObjectMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    public SamlFormView map(String url, String samlMessageType, AuthnRequest authnRequest, String submitTest) throws Throwable{
        String samlMessage = marshaller.marshallToString(authnRequest);
        String encodedSamlMessage = Base64.encodeAsString(samlMessage);
        return new SamlFormView(url, samlMessageType, encodedSamlMessage, submitTest);
    }
}
