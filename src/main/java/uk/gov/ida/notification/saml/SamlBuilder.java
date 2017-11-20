package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SAMLObject;

import javax.xml.namespace.QName;

public class SamlBuilder {
    public static <T extends SAMLObject> T build(QName qName) {
        return (T) XMLObjectSupport.buildXMLObject(qName);
    }
}
