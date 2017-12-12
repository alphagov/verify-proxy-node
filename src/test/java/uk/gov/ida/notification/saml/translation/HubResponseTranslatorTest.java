package uk.gov.ida.notification.saml.translation;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Response;
import se.litsec.eidas.opensaml.ext.attributes.EidasAttributeValueType;
import uk.gov.ida.notification.helpers.FileHelpers;
import uk.gov.ida.notification.saml.SamlParser;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import static junit.framework.TestCase.assertEquals;

public class HubResponseTranslatorTest {

    @BeforeClass
    public static void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldTranslateHubResponseToEidasResponse() throws Throwable {
        SamlParser samlParser = new SamlParser();
        HubResponseTranslator translator = new HubResponseTranslator("http://proxy-node.uk", "http://connector.eu", samlParser);
        HubResponse hubResponse = new HubResponse(getResponse("idp_response_unencrypted.xml"));
        Response expectedEidasResponse = getResponse("eidas_response.xml");

        Response actualEidasResponse = translator.translate(hubResponse);

        String expectedStatusCode = getStatusCode(expectedEidasResponse);
        String actualStatusCode = getStatusCode(actualEidasResponse);
        assertEquals(expectedStatusCode, actualStatusCode);
        String expectedLoa = getLoa(expectedEidasResponse);
        String actualLoa = getLoa(actualEidasResponse);
        assertEquals(expectedLoa, actualLoa);
        String expectedPid = getPid(expectedEidasResponse);
        String actualPid = getPid(actualEidasResponse);
        assertEquals(expectedPid, actualPid);
        assertEquals(expectedEidasResponse.getInResponseTo(), actualEidasResponse.getInResponseTo());
        List<String> expectedAttributes = getAttributes(expectedEidasResponse);
        List<String> actualAttributes = getAttributes(actualEidasResponse);
        assertThat(actualAttributes, containsInAnyOrder(expectedAttributes.toArray()));
    }

    private String getPid(Response expectedEidasResponse) {
        return expectedEidasResponse.getAssertions().get(0).getSubject().getNameID().getValue();
    }

    private String getLoa(Response expectedEidasResponse) {
        return expectedEidasResponse.getAssertions().get(0).getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
    }

    private String getStatusCode(Response expectedEidasResponse) {
        return expectedEidasResponse
                .getStatus()
                .getStatusCode()
                .getValue();
    }

    private List<String> getAttributes(Response response) {
        return response.getAssertions().get(0).getAttributeStatements().get(0).getAttributes()
                .stream()
                .map(a ->  ((EidasAttributeValueType) a.getAttributeValues().get(0)).toStringValue())
                .collect(Collectors.toList());
    }

    private Response getResponse(String resourceFilename) throws Exception {
        SamlParser parser = new SamlParser();
        FileHelpers.readFileAsString(resourceFilename);
        String xmlString = FileHelpers.readFileAsString(resourceFilename);
        return parser.parseSamlString(xmlString);
    }
}
