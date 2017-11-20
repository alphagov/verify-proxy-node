package uk.gov.ida.notification.saml;

import org.assertj.core.internal.Iterables;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.helpers.FileHelpers;

import javax.management.Attribute;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HubToEidasResponseTransformerTest {

    @BeforeClass
    public static void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void transform() throws Exception {
        Response hubResponse = getResponse("idp_response.xml");
        Response expectedEidasResponse = getResponse("eidas_response.xml");

        HubToEidasResponseTransformer transformer = new HubToEidasResponseTransformer();
        Response actualEidasResponse = transformer.transform(hubResponse);

        assertEquals(
                expectedEidasResponse.getStatus().getStatusCode().getValue(),
                actualEidasResponse.getStatus().getStatusCode().getValue());

//        assertEquals(
//                expectedEidasResponse.getAssertions().get(0).getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef(),
//                actualEidasResponse.getAssertions().get(0).getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef());
//
//        List<String> expectedAttributes = getAttributes(expectedEidasResponse);
//        List<String> actualAttributes = getAttributes(actualEidasResponse);
//
//        assertEquals(expectedAttributes, actualAttributes);
    }

    private List<String> getAttributes(Response response) {
        return response.getAssertions().get(0).getAttributeStatements().get(0).getAttributes()
                .stream()
                .map((a) -> a.getAttributeValues().get(0).getDOM().getTextContent())
                .collect(Collectors.toList());
    }

    private Response getResponse(String resourceFilename) throws Exception {
        SamlParser parser = new SamlParser();
        return (Response) parser.parseSamlString(
                FileHelpers.readFileAsString(resourceFilename)
        );
    }

}
