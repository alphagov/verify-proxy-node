package uk.gov.ida.notification.saml;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.impl.AuthnRequestImpl;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import uk.gov.ida.notification.helpers.FileHelpers;
import uk.gov.ida.notification.saml.SamlParser;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
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

    /**
     * Test for protection against the Billion Laughs attack.
     * @see https://en.wikipedia.org/wiki/Billion_laughs
     */
    @Test
    public void convertToElement_shouldDealWithEntityExpansionAttacks() throws Exception {
        String xmlString = "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE lolz [\n" +
                " <!ENTITY lol \"lol\">\n" +
                " <!ELEMENT lolz (#PCDATA)>\n" +
                " <!ENTITY lol1 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
                " <!ENTITY lol2 \"&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;\">\n" +
                " <!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n" +
                " <!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n" +
                " <!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n" +
                " <!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n" +
                " <!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n" +
                " <!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n" +
                " <!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n" +
                "]>\n" +
                "<lolz>&lol9;</lolz>";

        try {
            SamlParser parser = new SamlParser();
            parser.parseSamlString(xmlString);
            fail("expected exception not thrown");
        } catch(RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(SAXParseException.class);
        }
    }

    /**
     * Test to prevent XML External Entity processing (XXE attacks), i.e. access
     * to arbitrary files etc. on the processing system.
     * @see https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
     */
    @Test
    public void convertToElement_shouldThrowExceptionIfProvidedWithDoctypeDeclaration() throws Exception {
        String xmlString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
                "<!DOCTYPE foo [" +
                "  <!ELEMENT foo ANY >" +
                "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]><foo>&xxe;</foo>";
        try {
            SamlParser parser = new SamlParser();
            parser.parseSamlString(xmlString);
            fail("expected exception not thrown");
        } catch(RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(SAXParseException.class);
            assertThat(e.getMessage()).contains("DOCTYPE is disallowed");
        }
    }
}
