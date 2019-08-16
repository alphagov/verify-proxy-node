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

    private static final String XML_OBJECT_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><saml2:Issuer xmlns:saml2=\"{0}\" Format=\"{1}\">{2}</saml2:Issuer>";
    private static final SamlObjectMarshaller MARSHALLER = new SamlObjectMarshaller();

    private static final QName DEFAULT_ELEMENT_NAME = Issuer.DEFAULT_ELEMENT_NAME;
    private static final String ENTITY = NameIDType.ENTITY;
    private static final String ISSUER = "an-issuer";

    @Test
    public void shouldMarshallSamlObjectToString() {
        Issuer issuer = buildXmlObject(DEFAULT_ELEMENT_NAME, ENTITY, ISSUER);

        String issuerXML = MARSHALLER.transformToString(issuer);

        String expectedIssuerXML = buildExpectedXmlObjectFormat(DEFAULT_ELEMENT_NAME, ENTITY, ISSUER);
        assertThat(expectedIssuerXML).isEqualTo(issuerXML);
    }

    @Test
    public void shouldMarshallSamlObject() throws Throwable {
        Issuer issuer = buildXmlObject(DEFAULT_ELEMENT_NAME, ENTITY, ISSUER);
        assertThat(issuer.getDOM()).isEqualTo(null);

        Element element = MARSHALLER.marshallToElement(issuer);

        assertThat(element.getNamespaceURI()).isEqualTo(DEFAULT_ELEMENT_NAME.getNamespaceURI());
        assertThat(element.getLocalName()).isEqualTo("Issuer");
        assertThat(element.getFirstChild().getNodeValue()).isEqualTo(ISSUER);
        assertThat(element.getAttribute(ENTITY)).isNotNull();
        assertThat(element).isEqualTo(issuer.getDOM());
    }

    private Issuer buildXmlObject(QName defaultElementName, String entity, String aValue) {
        Issuer issuer = (Issuer) XMLObjectSupport.buildXMLObject(defaultElementName);
        issuer.setFormat(entity);
        issuer.setValue(aValue);
        return issuer;
    }

    private String buildExpectedXmlObjectFormat(QName elementName, String entity, String aValue) {
        return MessageFormat.format(XML_OBJECT_FORMAT, elementName.getNamespaceURI(), entity, aValue);
    }
}
