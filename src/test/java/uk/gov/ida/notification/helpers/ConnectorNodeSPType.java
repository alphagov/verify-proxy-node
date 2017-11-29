package uk.gov.ida.notification.helpers;

import se.litsec.eidas.opensaml.ext.SPType;
import se.litsec.eidas.opensaml.ext.impl.SPTypeImpl;

public class ConnectorNodeSPType extends SPTypeImpl implements SPType{

    public ConnectorNodeSPType() {
        super("namespaceURI", "elementLocalName", "namespacePrefix");
    }
}
