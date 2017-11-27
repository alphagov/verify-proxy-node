package uk.gov.ida.notification.saml;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.impl.AuthnRequestImpl;
import uk.gov.ida.notification.helpers.FileHelpers;
import uk.gov.ida.notification.saml.SamlParser;

import static org.junit.Assert.assertEquals;

public class SamlParserTest {
    @BeforeClass
    public static void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldParseAuthnRequest() throws Exception {
        SamlParser parser = new SamlParser();
        String testXML = FileHelpers.readFileAsString("eidas_authn_request.xml");
        assertEquals(AuthnRequestImpl.class, parser.parseSamlString(testXML).getClass());
    }
}
