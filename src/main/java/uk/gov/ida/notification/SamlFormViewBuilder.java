package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

public class SamlFormViewBuilder {
    private SamlObjectMarshaller marshaller = new SamlObjectMarshaller();

    public SamlFormView buildRequest(String url, AuthnRequest authnRequest, String submitText, String relayState) {
        String samlMessage = marshaller.transformToString(authnRequest);
        String encodedSamlMessage = Base64.encodeAsString(samlMessage);
        return new SamlFormView(url, SamlFormMessageType.SAML_REQUEST, encodedSamlMessage, submitText, relayState);
    }

    public SamlFormView buildResponse(String url, Response response, String submitText, String relayState) {
        String samlMessage = marshaller.transformToString(response);
        String encodedSamlMessage = Base64.encodeAsString(samlMessage);
        return new SamlFormView(url, SamlFormMessageType.SAML_RESPONSE, encodedSamlMessage, submitText, relayState);
    }
}
