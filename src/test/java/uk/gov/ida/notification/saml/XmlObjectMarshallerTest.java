package uk.gov.ida.notification.saml;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.text.MessageFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class XmlObjectMarshallerTest {

    private final String xmlObjectFormat = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><saml2:Issuer xmlns:saml2=\"{0}\" Format=\"{1}\">{2}</saml2:Issuer>";
    private XmlObjectMarshaller marshaller = new XmlObjectMarshaller();

    private final QName defaultElementName = Issuer.DEFAULT_ELEMENT_NAME;
    private final String entity = NameIDType.ENTITY;
    private final String aValue = "an-issuer";

    @Before
    public void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldMarshalSamlObjectToString() throws Throwable {
        Issuer issuer = buildXMLObject(defaultElementName, entity, aValue);

        String issuerXML = marshaller.transformToString(issuer);

        String expectedIssuerXML = buildExpectedXmlObjectFormat(defaultElementName, entity, aValue);
        assertEquals(expectedIssuerXML, issuerXML);
    }

    @Test
    public void shouldMarshalSamlObject() throws Throwable {
        Issuer issuer = buildXMLObject(defaultElementName, entity, aValue);
        assertEquals(issuer.getDOM(), null);

        Element element = marshaller.marshall(issuer);

        assertEquals(element.getNamespaceURI(), defaultElementName.getNamespaceURI() );
        assertEquals(element.getLocalName(), "Issuer");
        assertEquals(element.getFirstChild().getNodeValue(), aValue );
        assertNotNull(element.getAttribute(entity));
        assertEquals(element, issuer.getDOM());
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
