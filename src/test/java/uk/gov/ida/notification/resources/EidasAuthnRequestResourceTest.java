package uk.gov.ida.notification.resources;

import org.junit.Test;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.core.Form;
import java.io.IOException;
import java.net.URI;

import static junit.framework.TestCase.assertEquals;
import static org.glassfish.jersey.internal.util.Base64.encodeAsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EidasAuthnRequestResourceTest {

    private final String eidasAuthnRequest = "eidas authnrequest";
    private final String hubAuthnResponse = "hub authnrequest";
    private final String hubUrl = "http://hello.com";

    private EidasProxyNodeConfiguration configuration = mock(EidasProxyNodeConfiguration.class);
    private EidasAuthnRequestTranslator eidasAuthnRequestTranslator = mock(EidasAuthnRequestTranslator.class);
    private EidasAuthnRequestResource eidasAuthnRequestResource = new EidasAuthnRequestResource(configuration, eidasAuthnRequestTranslator);

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
        when(eidasAuthnRequestTranslator.translate(eidasAuthnRequest)).thenReturn(hubAuthnResponse);
    }

    private void ViewShouldHaveHubAuthnRequest(SamlFormView view) {
        assertEquals(view.getSamlMessageType(), SamlMessageType.SAML_REQUEST);
        assertEquals(view.getEncodedSamlMessage(), encodeAsString(hubAuthnResponse));
        assertEquals(view.getPostUrl(), hubUrl);
        assertEquals(view.getSubmitText(), "Post Verify Authn Request to Hub");
    }
}
