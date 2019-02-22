package uk.gov.ida.notification.resources;

import com.google.common.collect.Lists;
import org.junit.After;
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
import uk.gov.ida.notification.exceptions.SessionAttributeException;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.views.SamlFormView;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.session.SessionKeys.SESSION_KEY_SESSION_DATA;

@RunWith(MockitoJUnitRunner.class)
public class HubResponseResourceTest {
    @Mock
    TranslatorProxy translatorProxy;

    @Mock
    private HttpSession session;

    @Mock
    private Handler logHandler;

    @Captor
    private ArgumentCaptor<LogRecord> captorLoggingEvent;

    @Captor
    private ArgumentCaptor<HubResponseTranslatorRequest> requestCaptor;

    @Before
    public void setup() {
        Logger logger = Logger.getLogger(HubResponseResource.class.getName());
        logger.addHandler(logHandler);
    }

    @After
    public void teardown() {
        Logger logger = Logger.getLogger(HubResponseResource.class.getName());
        logger.removeHandler(logHandler);
    }

    @Test
    public void testsHappyPath() {
        EidasSamlParserResponse eidasSamlParserResponse = new EidasSamlParserResponse(
            "eidas_request_id_in_session",
            "issuer",
            "connector_public_cert_in_session",
            "http://conector.node"
        );

        AuthnRequestResponse vspResponse = new AuthnRequestResponse(
            "saml-request",
            "hub_request_id_in_session",
            UriBuilder.fromUri("http://conector.node").build()
        );

        GatewaySessionData sessionData = new GatewaySessionData(
            eidasSamlParserResponse,
            vspResponse,
            "eidas_relay_state_in_session"
        );

        when(session.getAttribute(SESSION_KEY_SESSION_DATA)).thenReturn(sessionData);
        when(session.getId()).thenReturn("session-id");
        when(translatorProxy.getTranslatedResponse(any(HubResponseTranslatorRequest.class), eq("session-id"))).thenReturn("translated_eidas_response");

        HubResponseResource resource =  new HubResponseResource(
            new SamlFormViewBuilder(),
            translatorProxy
        );

        SamlFormView response = (SamlFormView) resource.hubResponse("hub_saml_response", "relay_state", session);

        verify(translatorProxy).getTranslatedResponse(requestCaptor.capture(), eq("session-id"));
        HubResponseTranslatorRequest request = requestCaptor.getValue();

        verifyNoMoreInteractions(translatorProxy);
        assertEquals("hub_saml_response", request.getSamlResponse());
        assertEquals("hub_request_id_in_session", request.getRequestId());
        assertEquals(HubResponseResource.LEVEL_OF_ASSURANCE, request.getLevelOfAssurance());
        assertEquals("eidas_request_id_in_session", request.getEidasRequestId());
        assertEquals("connector_public_cert_in_session", request.getConnectorEncryptionCertificate());

        assertEquals("http://conector.node", response.getPostUrl());
        assertEquals("SAMLResponse", response.getSamlMessageType());
        assertEquals("translated_eidas_response", response.getEncodedSamlMessage());
        assertEquals(HubResponseResource.SUBMIT_TEXT, response.getSubmitText());
        assertEquals("eidas_relay_state_in_session", response.getRelayState());

        verify(logHandler, times(2)).publish(captorLoggingEvent.capture());
        List<String> allLogRecords = captorLoggingEvent
            .getAllValues()
            .stream()
            .map(m -> m.getMessage())
            .sorted()
            .collect(Collectors.toList());

        List<String> expectedLogOutput = Lists.newArrayList(
            "[HUB Response] received for session 'session-id', hub authn request ID 'hub_request_id_in_session', eIDAS authn request ID 'eidas_request_id_in_session'",
            "[eIDAS Response] received for session 'session-id', hub authn request ID 'hub_request_id_in_session', eIDAS authn request ID 'eidas_request_id_in_session'"
        );

        assertEquals(expectedLogOutput, allLogRecords);
    }

    @Test
    public void shouldThrowSamlAttributeErrorIfMissingSessionIsNull() {
        when(session.getAttribute(SESSION_KEY_SESSION_DATA)).thenReturn(null);

        HubResponseResource resource =  new HubResponseResource(
            new SamlFormViewBuilder(),
            translatorProxy
        );

        assertThatThrownBy(() -> { resource.hubResponse("hub_saml_response", "relay_state", session); })
            .isInstanceOf(SessionAttributeException.class)
            .hasMessage("Session data can not be null");
    }

    @Test
    public void shouldThrowSamlAttributeErrorIfMissingSessionAttribute() {
        EidasSamlParserResponse eidasSamlParserResponse = new EidasSamlParserResponse(
            "eidas_request_id_in_session",
            "issuer",
            "",
            null
        );

        AuthnRequestResponse vspResponse = new AuthnRequestResponse(
            "saml-request",
            "hub_request_id_in_session",
            UriBuilder.fromUri("http://conector.node").build()
        );

        GatewaySessionData sessionData = new GatewaySessionData(
            eidasSamlParserResponse,
            vspResponse,
            "eidas_relay_state_in_session"
        );

        when(session.getAttribute(SESSION_KEY_SESSION_DATA)).thenReturn(sessionData);

        HubResponseResource resource =  new HubResponseResource(
            new SamlFormViewBuilder(),
            translatorProxy
        );

        assertThatThrownBy(() -> { resource.hubResponse("hub_saml_response", "relay_state", session); })
            .isInstanceOf(SessionAttributeException.class)
            .hasMessage("eidasConnectorPublicKey field may not be empty, eidasDestination field may not be empty");
    }
}
