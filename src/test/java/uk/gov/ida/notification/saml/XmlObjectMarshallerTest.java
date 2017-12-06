package uk.gov.ida.notification.saml;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;

import javax.xml.namespace.QName;

import java.text.MessageFormat;

import static org.junit.Assert.assertEquals;

public class XmlObjectMarshallerTest {

    private final String xmlObjectFormat = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><saml2:Issuer xmlns:saml2=\"{0}\" Format=\"{1}\">{2}</saml2:Issuer>";

    @BeforeClass
    public static void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldMarshalSamlObjectToString() throws Throwable {
        QName defaultElementName = Issuer.DEFAULT_ELEMENT_NAME;
        String entity = NameIDType.ENTITY;
        String aValue = "an-issuer";
        Issuer issuer = buildXMLObject(defaultElementName, entity, aValue);
        XmlObjectMarshaller marshaller = new XmlObjectMarshaller();

        String issuerXML = marshaller.marshallToString(issuer);

        String expectedIssuerXML = buildExpectedXmlObjectFormat(defaultElementName, entity, aValue);
        assertEquals(expectedIssuerXML, issuerXML);
    }

    private Issuer buildXMLObject(QName defaultElementName, String entity, String aValue) {
        Issuer issuer = (Issuer) XMLObjectSupport.buildXMLObject(defaultElementName);
        issuer.setFormat(entity);
        issuer.setValue(aValue);
        return issuer;
    }

    private String buildExpectedXmlObjectFormat(QName elementName, String entity, String aValue) {
        return MessageFormat.format(xmlObjectFormat, elementName.getNamespaceURI(), entity, aValue);
    }
}
