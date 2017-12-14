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
    @Test
    public void shouldGenerateSAMLRequestForm() {
        SamlFormViewBuilder builder = new SamlFormViewBuilder();
        SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);
        String encodedAuthnRequest = Base64.encodeAsString(marshaller.transformToString(authnRequest));

        SamlFormView view = builder.buildRequest("url", authnRequest, "submit");

        assertEquals("url", view.getPostUrl());
        assertEquals(SamlFormMessageType.SAML_REQUEST, view.getSamlMessageType());
        assertEquals(encodedAuthnRequest, view.getEncodedSamlMessage());
        assertEquals("submit", view.getSubmitText());
    }

    @Test
    public void shouldGenerateSAMLResponseForm() {
        SamlFormViewBuilder builder = new SamlFormViewBuilder();
        SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
        Response response = SamlBuilder.build(Response.DEFAULT_ELEMENT_NAME);
        String encodedAuthnRequest = Base64.encodeAsString(marshaller.transformToString(response));

        SamlFormView view = builder.buildResponse("url", response, "submit");

        assertEquals("url", view.getPostUrl());
        assertEquals(SamlFormMessageType.SAML_RESPONSE, view.getSamlMessageType());
        assertEquals(encodedAuthnRequest, view.getEncodedSamlMessage());
        assertEquals("submit", view.getSubmitText());
    }
}
