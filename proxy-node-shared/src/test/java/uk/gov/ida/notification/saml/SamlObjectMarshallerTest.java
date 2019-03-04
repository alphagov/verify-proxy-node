package uk.gov.ida.notification.saml;

import org.junit.Test;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import org.w3c.dom.Element;
import uk.gov.ida.notification.SamlInitializedTest;

import javax.xml.namespace.QName;
import java.text.MessageFormat;

import static org.assertj.core.api.Assertions.assertThat;

public class SamlObjectMarshallerTest extends SamlInitializedTest {

    private final String xmlObjectFormat = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><saml2:Issuer xmlns:saml2=\"{0}\" Format=\"{1}\">{2}</saml2:Issuer>";
    private SamlObjectMarshaller marshaller = new SamlObjectMarshaller();

    private final QName defaultElementName = Issuer.DEFAULT_ELEMENT_NAME;
    private final String entity = NameIDType.ENTITY;
    private final String aValue = "an-issuer";

    @Test
    public void shouldMarshallSamlObjectToString() {
        Issuer issuer = buildXmlObject(defaultElementName, entity, aValue);

        String issuerXML = marshaller.transformToString(issuer);

        String expectedIssuerXML = buildExpectedXmlObjectFormat(defaultElementName, entity, aValue);
        assertThat(expectedIssuerXML).isEqualTo(issuerXML);
    }

    @Test
    public void shouldMarshallSamlObject() throws Throwable {
        Issuer issuer = buildXmlObject(defaultElementName, entity, aValue);
        assertThat(issuer.getDOM()).isEqualTo(null);

        Element element = marshaller.marshallToElement(issuer);

        assertThat(element.getNamespaceURI()).isEqualTo(defaultElementName.getNamespaceURI() );
        assertThat(element.getLocalName()).isEqualTo("Issuer");
        assertThat(element.getFirstChild().getNodeValue()).isEqualTo(aValue );
        assertThat(element.getAttribute(entity)).isNotNull();
        assertThat(element).isEqualTo(issuer.getDOM());
    }

    private Issuer buildXmlObject(QName defaultElementName, String entity, String aValue) {
        Issuer issuer = (Issuer) XMLObjectSupport.buildXMLObject(defaultElementName);
        issuer.setFormat(entity);
        issuer.setValue(aValue);
        return issuer;
    }

    private String buildExpectedXmlObjectFormat(QName elementName, String entity, String aValue) {
        return MessageFormat.format(xmlObjectFormat, elementName.getNamespaceURI(), entity, aValue);
    }
}
