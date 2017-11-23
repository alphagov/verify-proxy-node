package uk.gov.ida.notification.saml.translation;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import se.litsec.eidas.opensaml.ext.attributes.EidasAttributeValueType;
import uk.gov.ida.notification.saml.SamlBuilder;

import javax.xml.namespace.QName;
import java.util.function.Function;

public class EidasAttributeBuilder {
    private final String attributeName;
    private final String attributeFriendlyName;
    private final QName attributeValueTypeName;
    private final Function<HubResponse, String> attributeMapper;

    public EidasAttributeBuilder(String attributeName, String attributeFriendlyName, QName attributeValueTypeName, Function<HubResponse, String> attributeMapper) {
        this.attributeName = attributeName;
        this.attributeFriendlyName = attributeFriendlyName;
        this.attributeValueTypeName = attributeValueTypeName;
        this.attributeMapper = attributeMapper;
    }

    public Attribute build(HubResponse hubResponse) {
        Attribute attribute = SamlBuilder.build(Attribute.DEFAULT_ELEMENT_NAME);
        EidasAttributeValueType attributeValueType = SamlBuilder.build(attributeValueTypeName);

        attributeValueType.parseStringValue(attributeMapper.apply(hubResponse));
        attribute.getAttributeValues().add((XMLObject) attributeValueType);
        attribute.setName(attributeName);
        attribute.setFriendlyName(attributeFriendlyName);
        attribute.setNameFormat(Attribute.URI_REFERENCE);

        return attribute;
    }
}
