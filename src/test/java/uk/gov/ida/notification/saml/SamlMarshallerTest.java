package uk.gov.ida.notification.saml;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import uk.gov.ida.notification.helpers.FileHelpers;

import static org.junit.Assert.assertEquals;

public class SamlMarshallerTest {
    @BeforeClass
    public static void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void samlObjectToString() throws Exception {
        String expectedIssuerXML = FileHelpers.readFileAsString("saml_issuer.xml").trim();

        Issuer issuer = (Issuer) XMLObjectSupport.buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setFormat(NameIDType.ENTITY);
        issuer.setValue("an-issuer");

        SamlMarshaller marshaller = new SamlMarshaller();
        String issuerXML = marshaller.samlObjectToString(issuer);

        assertEquals(expectedIssuerXML, issuerXML);
    }
}
