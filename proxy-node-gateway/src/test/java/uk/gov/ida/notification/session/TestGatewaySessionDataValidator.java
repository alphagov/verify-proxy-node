package uk.gov.ida.notification.session;

import org.junit.Test;
import uk.gov.ida.notification.exceptions.SessionAttributeException;

import javax.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.session.GatewaySessionDataValidator.NOT_NULL_MESSAGE;
import static uk.gov.ida.notification.session.SessionKeys.SESSION_KEY_SESSION_DATA;

public class TestGatewaySessionDataValidator {

    @Test
    public void getValidatedSessionDataShouldReturnSessionData() {
        GatewaySessionData expectedSessionData = new GatewaySessionData(
            "hub-request-id",
            "eidas-request-id",
            "eidas-destination",
            "eidas-relay-state",
            "eidas-connector-public-key"
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

        try {
            GatewaySessionDataValidator.getValidatedSessionData(session);
            fail("Expected exception not thrown");
        } catch (SessionAttributeException e) {
            assertThat(e).hasMessage(NOT_NULL_MESSAGE);
        }
    }

    @Test
    public void getValidatedSessionDataShouldThrowSessionAttributeExceptionIfSAttributeIsNullOrEmpty() {
        GatewaySessionData expectedSessionData = new GatewaySessionData(
            "hub-request-id",
            "",
            null,
            "eidas-relay-state",
            "eidas-connector-public-key"
        );

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(SESSION_KEY_SESSION_DATA)).thenReturn(expectedSessionData);

        try {
            GatewaySessionDataValidator.getValidatedSessionData(session);
            fail("Expected exception not thrown");
        } catch (SessionAttributeException e) {
            assertThat(e).hasMessage("eidasDestination field may not be empty, eidasRequestId field may not be empty");
        }
    }
}
