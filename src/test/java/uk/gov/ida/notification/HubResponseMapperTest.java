package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.HubResponse;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HubResponseMapperTest {

    @Test
    public void shouldMapeWebStringToHubResponse() throws Throwable {
        String responseInputStringContent = "response";
        String encodedInput = Base64.encodeAsString(responseInputStringContent);
        Response response = buildParsedResponse();
        SamlParser parser = mock(SamlParser.class);
        when(parser.parseSamlString(responseInputStringContent)).thenReturn(response);
        HubResponseMapper hubResponseMapper = new HubResponseMapper(parser);

        HubResponse hubResponse = hubResponseMapper.map(encodedInput);

        assertNotEquals(null, hubResponse);
        assertEquals(hubResponse.getResponseId(), response.getID());
        assertEquals(hubResponse.getAuthnAssertion(), response.getAssertions().get(0));
        assertEquals(hubResponse.getMdsAssertion(), response.getAssertions().get(1));
    }

    private Response buildParsedResponse() {
        List<AuthnStatement> authnStatements = new ArrayList<>();
        List<Assertion> assertions = new ArrayList<>();
        Assertion authnAssertion = mock(Assertion.class, RETURNS_DEEP_STUBS);
        Assertion mdsAssertions = mock(Assertion.class);
        assertions.add(authnAssertion);
        assertions.add(mdsAssertions);
        AuthnStatement authnStatement = mock(AuthnStatement.class, RETURNS_DEEP_STUBS);
        authnStatements.add(authnStatement);
        Response response = mock(Response.class, RETURNS_DEEP_STUBS);
        List<AttributeStatement> attibuteStatements = new ArrayList<>();
        AttributeStatement attributeStatement = mock(AttributeStatement.class);
        attibuteStatements.add(attributeStatement);
        when(response.getAssertions()).thenReturn(assertions);
        when(response.getAssertions().get(0).getAuthnStatements()).thenReturn(authnStatements);
        when(response.getAssertions().get(1).getAuthnStatements()).thenReturn(new ArrayList<>());
        when(response.getAssertions().get(1).getAttributeStatements()).thenReturn(attibuteStatements);
        when(authnAssertion.getSubject().getNameID().getValue()).thenReturn("pid");
        when(authnAssertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()).thenReturn("loa");
        return response;
    }
}
