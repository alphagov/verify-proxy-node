package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.helpers.FileHelpers;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.HubResponse;

import static junit.framework.TestCase.assertEquals;

public class HubResponseGeneratorTest {
    @Before
    public void before() throws InitializationException {
        InitializationService.initialize();
    }

    @Test
    public void shouldGenerateHubResponseFromWebString() throws Throwable {
        // Replace with marshalled built response
        String responseInputStringContent = FileHelpers.readFileAsString("idp_response_unencrypted.xml");
        String encodedInput = Base64.encodeAsString(responseInputStringContent);
        SamlParser parser = new SamlParser();
        Response response = parser.parseSamlString(responseInputStringContent);

        HubResponseGenerator hubResponseGenerator = new HubResponseGenerator(parser);
        HubResponse hubResponse = hubResponseGenerator.generate(encodedInput);

        assertEquals("_d6895f24-1048-40e5-92fe-cafc6f1b9e3f", hubResponse.getResponseId());
    }
}
