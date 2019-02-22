package uk.gov.ida.notification.session;

import org.junit.Test;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;
import uk.gov.ida.notification.exceptions.SessionAttributeException;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.session.GatewaySessionDataValidator.NOT_NULL_MESSAGE;
import static uk.gov.ida.notification.session.SessionKeys.SESSION_KEY_SESSION_DATA;

public class TestGatewaySessionDataValidator {

    private final AuthnRequestResponse vspResponse = new AuthnRequestResponse(
        "saml-request",
        "hub-request-id",
        UriBuilder.fromUri("https://example.com").build()
    );

    @Test
    public void getValidatedSessionDataShouldReturnSessionData() {
        EidasSamlParserResponse eidasSamlParserResponse = new EidasSamlParserResponse(
            "hub-request-id",
            "issuer",
            "eidas-connector-public-key",
            "eidas-destination"
        );

        GatewaySessionData expectedSessionData = new GatewaySessionData(
            eidasSamlParserResponse,
            vspResponse,
            "eidas-relay-state"
        );

        HttpSession session = mock(HttpSession.class);

        when(session.getAttribute(SESSION_KEY_SESSION_DATA)).thenReturn(expectedSessionData);
        GatewaySessionData sessionData = GatewaySessionDataValidator.getValidatedSessionData(session);

        assertThat(sessionData).isEqualTo(expectedSessionData);
    }

    @Test
    public void getValidatedSessionDataShouldThrowSessionAttributeExceptionIfSessionDataIsNull() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(SESSION_KEY_SESSION_DATA)).thenReturn(null);

        assertThatThrownBy(() -> { GatewaySessionDataValidator.getValidatedSessionData(session); })
            .isInstanceOf(SessionAttributeException.class)
            .hasMessage(NOT_NULL_MESSAGE);
    }

    @Test
    public void getValidatedSessionDataShouldThrowSessionAttributeExceptionIfSAttributeIsNullOrEmpty() {
        EidasSamlParserResponse eidasSamlParserResponse = new EidasSamlParserResponse(
            null,
            "issuer",
            "eidas-connector-public-key",
            ""
        );

        GatewaySessionData expectedSessionData = new GatewaySessionData(
            eidasSamlParserResponse,
            vspResponse,
            "eidas-relay-state"
        );

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(SESSION_KEY_SESSION_DATA)).thenReturn(expectedSessionData);

        assertThatThrownBy(() -> { GatewaySessionDataValidator.getValidatedSessionData(session); })
            .isInstanceOf(SessionAttributeException.class)
            .hasMessage("eidasDestination field may not be empty, eidasRequestId field may not be empty");
    }
}
