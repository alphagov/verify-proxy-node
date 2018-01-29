package uk.gov.ida.notification;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.SamlBuilder;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

import static junit.framework.TestCase.assertEquals;

public class SamlFormViewBuilderTest extends SamlInitializedTest {

    private SamlFormViewBuilder builder = new SamlFormViewBuilder();
    private SamlObjectMarshaller marshaller = new SamlObjectMarshaller();

    @Test
    public void shouldGenerateSAMLRequestForm() {
        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);

        String encodedAuthnRequest = Base64.encodeAsString(marshaller.transformToString(authnRequest));

        SamlFormView view = builder.buildRequest("url", authnRequest, "submit", "relay");

        assertEquals(SamlFormMessageType.SAML_REQUEST, view.getSamlMessageType());
        assertEquals(encodedAuthnRequest, view.getEncodedSamlMessage());
        assertEquals("url", view.getPostUrl());
        assertEquals("submit", view.getSubmitText());
        assertEquals("relay", view.getRelayState());
    }

    @Test
    public void shouldGenerateSAMLResponseForm() {
        Response response = SamlBuilder.build(Response.DEFAULT_ELEMENT_NAME);

        String encodedResponse = Base64.encodeAsString(marshaller.transformToString(response));

        SamlFormView view = builder.buildResponse("url", response, "submit", "relay");

        assertEquals(SamlFormMessageType.SAML_RESPONSE, view.getSamlMessageType());
        assertEquals(encodedResponse, view.getEncodedSamlMessage());
        assertEquals("url", view.getPostUrl());
        assertEquals("submit", view.getSubmitText());
        assertEquals("relay", view.getRelayState());
    }
}
