package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import se.litsec.eidas.opensaml.ext.attributes.EidasAttributeValueType;
import uk.gov.ida.notification.saml.SamlBuilder;

import javax.xml.namespace.QName;

public class EidasAttributeBuilder {
    private final String attributeName;
    private final String attributeFriendlyName;
    private final QName attributeValueTypeName;
    private final String attributeAsString;

    public EidasAttributeBuilder(String attributeName, String attributeFriendlyName, QName attributeValueTypeName, String attributeAsString) {
        this.attributeName = attributeName;
        this.attributeFriendlyName = attributeFriendlyName;
        this.attributeValueTypeName = attributeValueTypeName;
        this.attributeAsString = attributeAsString;
    }

    public Attribute build() {
        XMLObjectBuilder<? extends EidasAttributeValueType> eidasTypeBuilder = (XMLObjectBuilder<? extends EidasAttributeValueType>) XMLObjectSupport.getBuilder(attributeValueTypeName);
        Attribute attribute = SamlBuilder.build(Attribute.DEFAULT_ELEMENT_NAME);
        EidasAttributeValueType attributeValue = eidasTypeBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, attributeValueTypeName);

        attributeValue.parseStringValue(attributeAsString);

        attribute.setName(attributeName);
        attribute.setFriendlyName(attributeFriendlyName);
        attribute.setNameFormat(Attribute.URI_REFERENCE);
        attribute.getAttributeValues().add((XMLObject) attributeValue);

        return attribute;
    }
}
