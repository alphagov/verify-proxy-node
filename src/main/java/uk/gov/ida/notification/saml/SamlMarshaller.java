package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLObject;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class SamlMarshaller {
    public String samlObjectToString(SAMLObject samlObject) {
        Element element;

        try {
            MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(samlObject);
            element = marshaller.marshall(samlObject);
        } catch (MarshallingException e) {
            throw new RuntimeException(e);
        }

        String xmlString;
        StringWriter output = new StringWriter();

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(element), new StreamResult(output));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

        xmlString = output.toString();
        return xmlString;
    }
}
