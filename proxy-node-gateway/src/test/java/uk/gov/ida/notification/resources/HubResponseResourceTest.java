package uk.gov.ida.notification.resources;

import io.prometheus.client.Counter;
import org.junit.Before;
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
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.views.SamlFormView;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubResponseResourceTest {

    @Mock
    private static TranslatorProxy translatorProxy;

    @Mock
    private static HttpSession session;

    @Mock
    private static SessionStore sessionStore;

    @Captor
    private static ArgumentCaptor<HubResponseTranslatorRequest> requestCaptor;

    private Counter RESPONSES;
    private Counter.Child RESPONSES_CHILD;
    private Counter RESPONSES_SUCCESSFUL;
    private Counter.Child RESPONSES_SUCCESSFUL_CHILD;

    @Before
    public void setup() throws Exception {
        RESPONSES = mock(Counter.class);
        RESPONSES_CHILD = mock(Counter.Child.class);
        RESPONSES_SUCCESSFUL = mock(Counter.class);
        RESPONSES_SUCCESSFUL_CHILD = mock(Counter.Child.class);
        when(RESPONSES.labels("http://entityId")).thenReturn(RESPONSES_CHILD);
        when(RESPONSES_SUCCESSFUL.labels("http://entityId")).thenReturn(RESPONSES_SUCCESSFUL_CHILD);
        setFinalStatic(HubResponseResource.class.getDeclaredField("RESPONSES"), RESPONSES);
        setFinalStatic(HubResponseResource.class.getDeclaredField("RESPONSES_SUCCESSFUL"), RESPONSES_SUCCESSFUL);
    }

    @Test
    public void testsHappyPath() {
        EidasSamlParserResponse eidasSamlParserResponse = new EidasSamlParserResponse(
            "eidas_request_id_in_session",
            "http://entityId",
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
        when(session.getId()).thenReturn("session-id");
        when(translatorProxy.getTranslatedHubResponse(any(HubResponseTranslatorRequest.class), eq("session-id"))).thenReturn("translated_eidas_response");

        HubResponseResource resource = new HubResponseResource(
            new SamlFormViewBuilder(),
            translatorProxy,
                sessionStore
        );

        SamlFormView response = (SamlFormView) resource.hubResponse("hub_saml_response", "relay_state", session);

        verify(translatorProxy).getTranslatedHubResponse(requestCaptor.capture(), eq("session-id"));
        HubResponseTranslatorRequest request = requestCaptor.getValue();

        verifyNoMoreInteractions(translatorProxy);
        assertThat("hub_saml_response").isEqualTo(request.getSamlResponse());
        assertThat("hub_request_id_in_session").isEqualTo(request.getRequestId());
        assertThat(HubResponseResource.LEVEL_OF_ASSURANCE).isEqualTo(request.getLevelOfAssurance());
        assertThat("eidas_request_id_in_session").isEqualTo(request.getEidasRequestId());
        assertThat(URI.create("http://entityId")).isEqualTo(request.getEidasIssuerEntityId());

        assertThat("http://connector.node").isEqualTo(response.getPostUrl());
        assertThat("SAMLResponse").isEqualTo(response.getSamlMessageType());
        assertThat("translated_eidas_response").isEqualTo(response.getEncodedSamlMessage());
        assertThat("eidas_relay_state_in_session").isEqualTo(response.getRelayState());

        verify(RESPONSES).labels("http://entityId");
        verify(RESPONSES_CHILD).inc();
        verify(RESPONSES_SUCCESSFUL).labels("http://entityId");
        verify(RESPONSES_SUCCESSFUL_CHILD).inc();

    }

    @Test(expected = SessionMissingException.class)
    public void shouldThrowSamlAttributeErrorIfMissingSessionIsNull() {
        when(sessionStore.getSession(eq(session.getId()))).thenThrow(SessionMissingException.class);

        HubResponseResource resource = new HubResponseResource(
            new SamlFormViewBuilder(),
            translatorProxy,
                sessionStore
        );

        resource.hubResponse("hub_saml_response", "relay_state", session);
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
