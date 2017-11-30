package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.helpers.ConnectorNodeIssuer;
import uk.gov.ida.notification.helpers.ConnectorNodeSPType;
import uk.gov.ida.notification.resources.EidasAuthnRequestResource;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequestTranslator;
import uk.gov.ida.notification.views.SamlFormView;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static org.glassfish.jersey.internal.util.Base64.encodeAsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EidasAuthenticationResourceTest {

    private final String eidasAuthnRequestAsString = "eidas authnrequest";
    private final String hubAuthnRequestAsString = "hub authnrequest";
    private final String expectedEndodedSamlMessage = encodeAsString(hubAuthnRequestAsString);
    private final String hubUrl = "http://hello.com";
    private final String expectedSubmitText = "Post Verify Authn Request to Hub";
    private String samlRequest = SamlMessageType.SAML_REQUEST;

    private EidasProxyNodeConfiguration configuration = mock(EidasProxyNodeConfiguration.class);
    private final AuthnRequest hubAuthnRequest = mock(AuthnRequest.class);
    private final AuthnRequest authnRequest = buildAuthnRequest("any id");
    private EidasAuthnRequestTranslator eidasAuthnRequestTranslator = mock(EidasAuthnRequestTranslator.class);
    private SamlFormViewMapper samlFormViewMapper = mock(SamlFormViewMapper.class);
    private SamlParser parser = mock(SamlParser.class);
    private EidasAuthnRequestResource eidasAuthnRequestResource = new EidasAuthnRequestResource(
            configuration,
            eidasAuthnRequestTranslator,
            samlFormViewMapper,
            parser);

    @Test
    public void shouldGetViewWithHubRequestFromPost() throws IOException {
        SetupResourceDepenciesForSuccess();

        SamlFormView view = (SamlFormView) eidasAuthnRequestResource.handlePostBinding(encodeAsString(eidasAuthnRequestAsString));

        ViewShouldHaveHubAuthnRequest(view);
    }

    @Test
    public void shouldGetViewWithHubRequestFromRedirect() throws IOException {
        SetupResourceDepenciesForSuccess();

        SamlFormView view = (SamlFormView) eidasAuthnRequestResource.handleRedirectBinding(encodeAsString(eidasAuthnRequestAsString));

        ViewShouldHaveHubAuthnRequest(view);
    }

    private void SetupResourceDepenciesForSuccess() {
        when(configuration.getHubUrl()).thenReturn(URI.create(hubUrl));
        when(eidasAuthnRequestTranslator.translate(any(EidasAuthnRequest.class))).thenReturn(hubAuthnRequest);
        when(parser.parseSamlString(eidasAuthnRequestAsString, AuthnRequest.class)).thenReturn(authnRequest);
        when(samlFormViewMapper.map(hubUrl, samlRequest, hubAuthnRequest, expectedSubmitText)).thenReturn(new SamlFormView(hubUrl, samlRequest, expectedEndodedSamlMessage, expectedSubmitText));
    }

    private void ViewShouldHaveHubAuthnRequest(SamlFormView view) {
        assertEquals(view.getSamlMessageType(), samlRequest);
        assertEquals(view.getEncodedSamlMessage(), expectedEndodedSamlMessage);
        assertEquals(view.getPostUrl(), hubUrl);
        assertEquals(view.getSubmitText(), expectedSubmitText);
    }

    private AuthnRequest buildAuthnRequest(String requestId) {
        String loa = EidasConstants.EIDAS_LOA_SUBSTANTIAL;
        AuthnRequest authnRequest = mock(AuthnRequest.class, RETURNS_DEEP_STUBS);
        when(authnRequest.getIssuer()).thenReturn(new ConnectorNodeIssuer());
        when(authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef()).thenReturn(loa);
        Extensions extentions = mock(Extensions.class);
        ArrayList<XMLObject> extentionChildren = new ArrayList<>();
        extentionChildren.add(new ConnectorNodeSPType());
        when(extentions.getOrderedChildren()).thenReturn(extentionChildren);
        when(authnRequest.getExtensions()).thenReturn(extentions);
        when(authnRequest.getID()).thenReturn(requestId);
        return authnRequest;
    }

}
