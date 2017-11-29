package uk.gov.ida.notification.helpers;

import org.opensaml.saml.saml2.core.impl.ExtensionsImpl;

public class ConnectorNodeExtentions extends ExtensionsImpl {

    public ConnectorNodeExtentions(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }
}
