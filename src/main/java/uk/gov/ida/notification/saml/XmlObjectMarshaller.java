package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.saml.common.SAMLObject;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class XmlObjectMarshaller {

    public String marshallToString(SAMLObject samlObject) throws Throwable{
        Element element = mashall(samlObject);
        return marshallToString(element);
    }

    private String marshallToString(Element element) throws Throwable {
        StringWriter output = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(element), new StreamResult(output));
        return output.toString();
    }

    private Element mashall(SAMLObject samlObject) throws Throwable {
        MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(samlObject);
        return marshaller.marshall(samlObject);
    }
}
