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

    private static final SamlFormViewBuilder SAML_FORM_VIEW_BUILDER = new SamlFormViewBuilder();
    private static final SamlObjectMarshaller MARSHALLER = new SamlObjectMarshaller();

    @Test
    public void shouldGenerateSAMLRequestForm() {
        AuthnRequest authnRequest = SamlBuilder.build(AuthnRequest.DEFAULT_ELEMENT_NAME);

        String encodedAuthnRequest = Base64.encodeAsString(MARSHALLER.transformToString(authnRequest));

        SamlFormView view = SAML_FORM_VIEW_BUILDER.buildRequest("url", authnRequest, "relay");

        assertThat(SamlFormMessageType.SAML_REQUEST).isEqualTo(view.getSamlMessageType());
        assertThat(encodedAuthnRequest).isEqualTo(view.getEncodedSamlMessage());
        assertThat("url").isEqualTo(view.getPostUrl());
        assertThat("relay").isEqualTo(view.getRelayState());
    }

    @Test
    public void shouldGenerateSAMLRequestFormFromEncodedSAMLMessage() {
        String encodedAuthnRequest = Base64.encodeAsString("a saml blob");
        SamlFormView view = SAML_FORM_VIEW_BUILDER.buildRequest("url", encodedAuthnRequest, "relay");
        assertThat(SamlFormMessageType.SAML_REQUEST).isEqualTo(view.getSamlMessageType());
        assertThat(encodedAuthnRequest).isEqualTo(view.getEncodedSamlMessage());
        assertThat("url").isEqualTo(view.getPostUrl());
        assertThat("relay").isEqualTo(view.getRelayState());
    }

    @Test
    public void shouldGenerateSAMLResponseForm() {
        Response response = SamlBuilder.build(Response.DEFAULT_ELEMENT_NAME);

        String encodedResponse = Base64.encodeAsString(MARSHALLER.transformToString(response));

        SamlFormView view = SAML_FORM_VIEW_BUILDER.buildResponse("url", response, "relay");

        assertThat(SamlFormMessageType.SAML_RESPONSE).isEqualTo(view.getSamlMessageType());
        assertThat(encodedResponse).isEqualTo(view.getEncodedSamlMessage());
        assertThat("url").isEqualTo(view.getPostUrl());
        assertThat("relay").isEqualTo(view.getRelayState());
    }

    @Test
    public void shouldGenerateSAMLResponseFormFromEncodedSAMLMessage() {
        String encodedResponse = Base64.encodeAsString("a response saml blob");

        SamlFormView view = SAML_FORM_VIEW_BUILDER.buildResponse("url", encodedResponse, "relay");

        assertThat(SamlFormMessageType.SAML_RESPONSE).isEqualTo(view.getSamlMessageType());
        assertThat(encodedResponse).isEqualTo(view.getEncodedSamlMessage());
        assertThat("url").isEqualTo(view.getPostUrl());
        assertThat("relay").isEqualTo(view.getRelayState());
    }
}
