package uk.gov.ida.notification.session;

import org.junit.Test;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;
import uk.gov.ida.notification.exceptions.SessionAttributeException;

import javax.ws.rs.core.UriBuilder;

public class TestGatewaySessionDataValidator {

    private static final AuthnRequestResponse vspResponse = new AuthnRequestResponse(
            "saml-request",
            "hub-request-id",
            UriBuilder.fromUri("https://example.com").build()
    );

    @Test
    public void getValidatedSessionDataShouldReturnSessionData() {
        EidasSamlParserResponse eidasSamlParserResponse = new EidasSamlParserResponse(
                "hub-request-id",
                "issuer",
                "eidas-destination",
                false
        );

        new GatewaySessionData(
                eidasSamlParserResponse,
                vspResponse,
                "eidas-relay-state"
        );
    }

    @Test(expected = SessionAttributeException.class)
    public void getValidatedSessionDataShouldThrowSessionAttributeExceptionIfSAttributeIsNullOrEmpty() {
        EidasSamlParserResponse eidasSamlParserResponse = new EidasSamlParserResponse(
                null,
                "issuer",
                "",
                false
        );

        new GatewaySessionData(
                eidasSamlParserResponse,
                vspResponse,
                "eidas-relay-state"
        );
    }
}
