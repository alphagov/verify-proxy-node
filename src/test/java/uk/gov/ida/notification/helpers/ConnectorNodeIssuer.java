package uk.gov.ida.notification.helpers;

import org.opensaml.saml.saml2.core.impl.IssuerImpl;

public class ConnectorNodeIssuer extends IssuerImpl {

    public ConnectorNodeIssuer() {
        super("namespaceURI", "elementLocalName", "namespacePrefix");
    }
}
