package uk.gov.ida.notification.saml.translation;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import se.litsec.eidas.opensaml.ext.attributes.EidasAttributeValueType;
import uk.gov.ida.notification.saml.SamlBuilder;

import javax.xml.namespace.QName;
import java.util.function.Function;

public class EidasAttributeBuilder {
    private final String attributeName;
    private final String attributeFriendlyName;
    private final QName attributeValueTypeName;
    private final Function<HubResponseContainer, String> attributeMapper;

    public EidasAttributeBuilder(String attributeName, String attributeFriendlyName, QName attributeValueTypeName, Function<HubResponseContainer, String> attributeMapper) {
        this.attributeName = attributeName;
        this.attributeFriendlyName = attributeFriendlyName;
        this.attributeValueTypeName = attributeValueTypeName;
        this.attributeMapper = attributeMapper;
    }

    public Attribute build(HubResponseContainer hubResponseContainer) {
        XMLObjectBuilder<? extends EidasAttributeValueType> eidasTypeBuilder = (XMLObjectBuilder<? extends EidasAttributeValueType>) XMLObjectSupport.getBuilder(attributeValueTypeName);
        Attribute attribute = SamlBuilder.build(Attribute.DEFAULT_ELEMENT_NAME);
        EidasAttributeValueType attributeValue = eidasTypeBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, attributeValueTypeName);

        attributeValue.parseStringValue(attributeMapper.apply(hubResponseContainer));

        attribute.setName(attributeName);
        attribute.setFriendlyName(attributeFriendlyName);
        attribute.setNameFormat(Attribute.URI_REFERENCE);
        attribute.getAttributeValues().add((XMLObject) attributeValue);

        return attribute;
    }
}
