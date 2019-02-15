package uk.gov.ida.notification.apprule;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class EidasAuthnRequestAppRuleTests extends GatewayAppRuleTestBase {

    private SamlParser parser;

    @Before
    public void setup() throws Throwable {
        parser = new SamlParser();
    }

    @Test
    public void bindingsReturnHubAuthnRequestForm() throws Throwable {
        assertGoodRequest(new EidasAuthnRequestBuilder().withIssuer(CONNECTOR_NODE_ENTITY_ID));
    }

    private AuthnRequest getHubAuthnRequestFromHtml(String html) throws IOException {
        String decodedHubAuthnRequest = HtmlHelpers.getValueFromForm(html, SamlFormMessageType.SAML_REQUEST);
        return parser.parseSamlString(decodedHubAuthnRequest);
    }

    private void assertGoodRequest(EidasAuthnRequestBuilder builder) throws Throwable {
        AuthnRequest postedRequest = builder.withRandomRequestId().build();
        assertGoodResponse(postEidasAuthnRequest(postedRequest));
        AuthnRequest redirectedRequest = builder.withRandomRequestId().build();
        assertGoodResponse(redirectEidasAuthnRequest(redirectedRequest));
    }

    private void assertGoodResponse(Response response) throws IOException {
        String html = response.readEntity(String.class);
        AuthnRequest hubAuthnRequest = getHubAuthnRequestFromHtml(html);
        assertEquals("a hub request id", hubAuthnRequest.getID());
    }
}