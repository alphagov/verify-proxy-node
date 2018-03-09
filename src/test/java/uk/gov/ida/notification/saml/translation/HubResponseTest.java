package uk.gov.ida.notification.saml.translation;

import org.junit.Test;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.helpers.FileHelpers;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.saml.security.validators.ValidatedResponse;

import static junit.framework.TestCase.assertEquals;

public class HubResponseTest extends SamlInitializedTest {
    @Test
    public void shouldGenerateHubResponseFromValidatedResponse() throws Throwable {
        // Replace with marshalled built response
        SamlParser parser = new SamlParser();
        Response response = parser.parseSamlString(FileHelpers.readFileAsString("idp_response_unencrypted.xml"));
        ValidatedResponse validatedResponse = new ValidatedResponse(response);

        HubResponse hubResponse = HubResponse.from(validatedResponse);

        assertEquals("_d6895f24-1048-40e5-92fe-cafc6f1b9e3f", hubResponse.getResponseId());
    }
}
