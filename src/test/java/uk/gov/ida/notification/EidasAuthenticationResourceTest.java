package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.views.SamlFormView;

import java.io.IOException;
import java.net.URI;

import static junit.framework.TestCase.assertEquals;
import static org.glassfish.jersey.internal.util.Base64.encodeAsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EidasAuthenticationResourceTest {

    private final String eidasAuthnRequestAsString = "eidas authnrequest";
    private final String encodedEidasAuthnRequest = encodeAsString(eidasAuthnRequestAsString);
    private final String hubAuthnRequestAsString = "hub authnrequest";
    private final String expectedEndodedSamlMessage = encodeAsString(hubAuthnRequestAsString);
    private final String hubUrl = "http://hello.com";
    private final String expectedSubmitText = "Post Verify Authn Request to Hub";
    private String samlRequest = SamlMessageType.SAML_REQUEST;

    private EidasProxyNodeConfiguration configuration = mock(EidasProxyNodeConfiguration.class);
    private final AuthnRequest hubAuthnRequest = mock(AuthnRequest.class);
    private final EidasAuthnRequest eidasAuthnRequest = mock(EidasAuthnRequest.class);
    private EidasAuthnRequestTranslator eidasAuthnRequestTranslator = mock(EidasAuthnRequestTranslator.class);
    private SamlFormViewMapper samlFormViewMapper = mock(SamlFormViewMapper.class);
    private EidasAuthnRequestMapper eidasMapper = mock(EidasAuthnRequestMapper.class);
    private EidasAuthnRequestResource eidasAuthnRequestResource = new EidasAuthnRequestResource(
            configuration,
            eidasAuthnRequestTranslator,
            samlFormViewMapper,
            eidasMapper);

    @Test
    public void shouldGetViewWithHubRequestFromPost() throws IOException {
        SetupResourceDepenciesForSuccess();

        SamlFormView view = (SamlFormView) eidasAuthnRequestResource.handlePostBinding(encodedEidasAuthnRequest);

        ViewShouldHaveHubAuthnRequest(view);
    }

    @Test
    public void shouldGetViewWithHubRequestFromRedirect() throws IOException {
        SetupResourceDepenciesForSuccess();

        SamlFormView view = (SamlFormView) eidasAuthnRequestResource.handleRedirectBinding(encodedEidasAuthnRequest);

        ViewShouldHaveHubAuthnRequest(view);
    }

    private void SetupResourceDepenciesForSuccess() {
        when(configuration.getHubUrl()).thenReturn(URI.create(hubUrl));
        when(eidasMapper.map(encodedEidasAuthnRequest)).thenReturn(eidasAuthnRequest);
        when(eidasAuthnRequestTranslator.translate(eidasAuthnRequest)).thenReturn(hubAuthnRequest);
        when(samlFormViewMapper.map(hubUrl, samlRequest, hubAuthnRequest, expectedSubmitText)).thenReturn(new SamlFormView(hubUrl, samlRequest, expectedEndodedSamlMessage, expectedSubmitText));
    }

    private void ViewShouldHaveHubAuthnRequest(SamlFormView view) {
        assertEquals(view.getSamlMessageType(), samlRequest);
        assertEquals(view.getEncodedSamlMessage(), expectedEndodedSamlMessage);
        assertEquals(view.getPostUrl(), hubUrl);
        assertEquals(view.getSubmitText(), expectedSubmitText);
    }
}