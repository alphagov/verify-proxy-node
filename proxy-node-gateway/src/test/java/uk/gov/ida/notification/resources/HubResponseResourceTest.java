package uk.gov.ida.notification.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;
import uk.gov.ida.notification.exceptions.SessionMissingException;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.SessionCookieService;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.views.SamlFormView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubResponseResourceTest {

    @Mock
    TranslatorProxy translatorProxy;

    @Mock
    private HttpSession session;

    @Mock
    private SessionStore sessionStore;

    @Mock
    private SessionCookieService sessionCookieService;

    @Captor
    private ArgumentCaptor<HubResponseTranslatorRequest> requestCaptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private EntityMetadata entityMetadata;

    @Mock
    private Cookie cookie;

    @Test
    public void testsHappyPath() {
        EidasSamlParserResponse eidasSamlParserResponse = new EidasSamlParserResponse(
            "eidas_request_id_in_session",
            "issuer",
            "connector_public_cert_in_session",
            "http://connector.node"
        );

        AuthnRequestResponse vspResponse = new AuthnRequestResponse(
            "saml-request",
            "hub_request_id_in_session",
            UriBuilder.fromUri("http://connector.node").build()
        );

        GatewaySessionData sessionData = new GatewaySessionData(
            eidasSamlParserResponse,
            vspResponse,
            "eidas_relay_state_in_session"
        );

        when(sessionStore.getSession(eq("session-id"))).thenReturn(sessionData);
        when(request.getSession()).thenReturn(session);
        when(session.getId()).thenReturn("session-id");
        when(translatorProxy.getTranslatedHubResponse(any(HubResponseTranslatorRequest.class), eq("session-id"))).thenReturn("translated_eidas_response");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        HubResponseResource resource = new HubResponseResource(
            new SamlFormViewBuilder(),
            translatorProxy,
                sessionStore,
                sessionCookieService,
                entityMetadata);

        SamlFormView response = (SamlFormView) resource.hubResponse("hub_saml_response", "relay_state", request);

        verify(translatorProxy).getTranslatedHubResponse(requestCaptor.capture(), eq("session-id"));
        HubResponseTranslatorRequest request = requestCaptor.getValue();

        verify(sessionCookieService).getData(new Cookie[]{cookie});
        verify(entityMetadata).getValue("issuer", EntityMetadata.Key.eidasDestination);
        verify(entityMetadata).getValue("issuer", EntityMetadata.Key.encryptionCertificate);
        verifyNoMoreInteractions(translatorProxy);
        assertThat("hub_saml_response").isEqualTo(request.getSamlResponse());
        assertThat("hub_request_id_in_session").isEqualTo(request.getRequestId());
        assertThat(HubResponseResource.LEVEL_OF_ASSURANCE).isEqualTo(request.getLevelOfAssurance());
        assertThat("eidas_request_id_in_session").isEqualTo(request.getEidasRequestId());
        assertThat("connector_public_cert_in_session").isEqualTo(request.getConnectorEncryptionCertificate());

        assertThat("http://connector.node").isEqualTo(response.getPostUrl());
        assertThat("SAMLResponse").isEqualTo(response.getSamlMessageType());
        assertThat("translated_eidas_response").isEqualTo(response.getEncodedSamlMessage());
        assertThat(HubResponseResource.SUBMIT_TEXT).isEqualTo(response.getSubmitText());
        assertThat("eidas_relay_state_in_session").isEqualTo(response.getRelayState());

    }

    @Test(expected = SessionMissingException.class)
    public void shouldThrowSamlAttributeErrorIfMissingSessionIsNull() {
        when(request.getSession()).thenReturn(session);
        when(sessionStore.getSession(eq(session.getId()))).thenThrow(SessionMissingException.class);

        HubResponseResource resource = new HubResponseResource(
            new SamlFormViewBuilder(),
            translatorProxy,
                sessionStore,
                sessionCookieService,
                entityMetadata);

        resource.hubResponse("hub_saml_response", "relay_state", request);
        verify(request).getSession();
    }
}
