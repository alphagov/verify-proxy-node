package uk.gov.ida.notification;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.SamlBuilder;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.views.SamlFormView;

import static org.assertj.core.api.Assertions.assertThat;

public class SamlFormViewBuilderTest extends SamlInitializedTest {

    private SamlFormViewBuilder builder = new SamlFormViewBuilder();
    private SamlObjectMarshaller marshaller = new SamlObjectMarshaller();

    @Test
    public void shouldGenerateSAMLRequestForm() {
        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);

        String encodedAuthnRequest = Base64.encodeAsString(marshaller.transformToString(authnRequest));

        SamlFormView view = builder.buildRequest("url", authnRequest, "submit", "relay");

        assertThat(SamlFormMessageType.SAML_REQUEST).isEqualTo(view.getSamlMessageType());
        assertThat(encodedAuthnRequest).isEqualTo(view.getEncodedSamlMessage());
        assertThat("url").isEqualTo(view.getPostUrl());
        assertThat("submit").isEqualTo(view.getSubmitText());
        assertThat("relay").isEqualTo(view.getRelayState());
    }

    @Test
    public void shouldGenerateSAMLRequestFormFromEncodedSAMLMessage() {
        String encodedAuthnRequest = Base64.encodeAsString("a saml blob");
        SamlFormView view = builder.buildRequest("url", encodedAuthnRequest, "submit", "relay");
        assertThat(SamlFormMessageType.SAML_REQUEST).isEqualTo(view.getSamlMessageType());
        assertThat(encodedAuthnRequest).isEqualTo(view.getEncodedSamlMessage());
        assertThat("url").isEqualTo(view.getPostUrl());
        assertThat("submit").isEqualTo(view.getSubmitText());
        assertThat("relay").isEqualTo(view.getRelayState());
    }

    @Test
    public void shouldGenerateSAMLResponseForm() {
        Response response = SamlBuilder.build(Response.DEFAULT_ELEMENT_NAME);

        String encodedResponse = Base64.encodeAsString(marshaller.transformToString(response));

        SamlFormView view = builder.buildResponse("url", response, "submit", "relay");

        assertThat(SamlFormMessageType.SAML_RESPONSE).isEqualTo(view.getSamlMessageType());
        assertThat(encodedResponse).isEqualTo(view.getEncodedSamlMessage());
        assertThat("url").isEqualTo(view.getPostUrl());
        assertThat("submit").isEqualTo(view.getSubmitText());
        assertThat("relay").isEqualTo(view.getRelayState());
    }

    @Test
    public void shouldGenerateSAMLResponseFormFromEncodedSAMLMessage() {

        String encodedResponse = Base64.encodeAsString("a response saml blob");

        SamlFormView view = builder.buildResponse("url", encodedResponse, "submit", "relay");

        assertThat(SamlFormMessageType.SAML_RESPONSE).isEqualTo(view.getSamlMessageType());
        assertThat(encodedResponse).isEqualTo(view.getEncodedSamlMessage());
        assertThat("url").isEqualTo(view.getPostUrl());
        assertThat("submit").isEqualTo(view.getSubmitText());
        assertThat("relay").isEqualTo(view.getRelayState());
    }
}
