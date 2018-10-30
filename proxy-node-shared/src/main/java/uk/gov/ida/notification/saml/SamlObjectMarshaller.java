package uk.gov.ida.notification.saml;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLObject;
import org.w3c.dom.Element;
import uk.gov.ida.notification.exceptions.saml.SamlMarshallingException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class SamlObjectMarshaller {
    public String transformToString(SAMLObject samlObject) {
        try {
            marshallToElement(samlObject);
            return transformToString(samlObject.getDOM());
        } catch (MarshallingException | TransformerException e) {
            throw new SamlMarshallingException(e);
        }
    }

    public Element marshallToElement(SAMLObject samlObject) throws MarshallingException {
        MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(samlObject);
        return marshaller.marshall(samlObject);
    }

    private String transformToString(Element element) throws TransformerException {
        StringWriter output = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(element), new StreamResult(output));
        return output.toString();
    }
}
