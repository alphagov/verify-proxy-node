package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.saml.SamlMarshaller;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.views.SamlFormView;

import java.io.IOException;
import java.net.URI;

import static junit.framework.TestCase.assertEquals;
import static org.glassfish.jersey.internal.util.Base64.encodeAsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EidasAuthenticationResourceTest {

    private final String eidasAuthnRequest = "eidas authnrequest";
    private final String hubAuthnRequestAsString = "hub authnrequest";
    private final String hubUrl = "http://hello.com";
    private EidasProxyNodeConfiguration configuration = mock(EidasProxyNodeConfiguration.class);

    private final AuthnRequest hubAuthnRequest = mock(AuthnRequest.class);
    private EidasAuthnRequestTranslator eidasAuthnRequestTranslator = mock(EidasAuthnRequestTranslator.class);
    private SamlMarshaller marshaller = mock(SamlMarshaller.class);
    private EidasAuthnRequestResource eidasAuthnRequestResource = new EidasAuthnRequestResource(
            configuration,
            eidasAuthnRequestTranslator,
            marshaller);

    @Test
    public void shouldGetViewWithHubRequestFromPost() throws IOException {
        SetupResourceDepenciesForSuccess();

        SamlFormView view = (SamlFormView) eidasAuthnRequestResource.handlePostBinding(encodeAsString(eidasAuthnRequest));

        ViewShouldHaveHubAuthnRequest(view);
    }

    @Test
    public void shouldGetViewWithHubRequestFromRedirect() throws IOException {
        SetupResourceDepenciesForSuccess();

        SamlFormView view = (SamlFormView) eidasAuthnRequestResource.handleRedirectBinding(encodeAsString(eidasAuthnRequest));

        ViewShouldHaveHubAuthnRequest(view);
    }

    private void SetupResourceDepenciesForSuccess() {
        when(configuration.getHubUrl()).thenReturn(URI.create(hubUrl));
        when(eidasAuthnRequestTranslator.translate(eidasAuthnRequest)).thenReturn(hubAuthnRequest);
        when(marshaller.samlObjectToString(hubAuthnRequest)).thenReturn(hubAuthnRequestAsString);
    }

    private void ViewShouldHaveHubAuthnRequest(SamlFormView view) {
        assertEquals(view.getSamlMessageType(), SamlMessageType.SAML_REQUEST);
        assertEquals(view.getEncodedSamlMessage(), encodeAsString(hubAuthnRequestAsString));
        assertEquals(view.getPostUrl(), hubUrl);
        assertEquals(view.getSubmitText(), "Post Verify Authn Request to Hub");
    }
}
