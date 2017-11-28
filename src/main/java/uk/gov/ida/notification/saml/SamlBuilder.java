package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.XMLObjectSupport;

import javax.xml.namespace.QName;

public class SamlBuilder {
    public static <T extends XMLObject> T build(QName elementName) {
        return (T) XMLObjectSupport.buildXMLObject(elementName);
    }

    public static <T extends XMLObject> T build(QName elementName, QName typeName) {
        return (T) XMLObjectSupport.buildXMLObject(elementName, typeName);
    }
}
