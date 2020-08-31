package uk.gov.ida.notification;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.Base64;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

public class SamlFormViewBuilder {
    private SamlObjectMarshaller marshaller = new SamlObjectMarshaller();

    public SamlFormView buildRequest(String url, AuthnRequest authnRequest, String relayState) {
        String samlMessage = marshaller.transformToString(authnRequest);
        String encodedSamlMessage = Base64.encodeToString(samlMessage);
        return buildRequest(url, encodedSamlMessage, relayState);
    }

    public SamlFormView buildRequest(String url, String encodedSamlMessage, String relayState) {
        return new SamlFormView(url, SamlFormMessageType.SAML_REQUEST, encodedSamlMessage, relayState);
    }

    public SamlFormView buildResponse(String url, Response response, String relayState) {
        String samlMessage = marshaller.transformToString(response);
        String encodedSamlMessage = Base64.encodeToString(samlMessage);
        return buildResponse(url, encodedSamlMessage, relayState);
    }

    public SamlFormView buildResponse(String url, String encodedSamlMessage, String relayState) {
        return new SamlFormView(url, SamlFormMessageType.SAML_RESPONSE, encodedSamlMessage, relayState);
    }
}
