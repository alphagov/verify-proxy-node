package uk.gov.ida.notification.helpers;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObject;
import org.w3c.dom.Element;

public class SamlMarshallerHelper {

    public Element mashall(SAMLObject samlObject) throws Throwable {
        return XMLObjectProviderRegistrySupport.getMarshallerFactory()
                .getMarshaller(samlObject)
                .marshall(samlObject);
    }
}
