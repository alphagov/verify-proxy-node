package uk.gov.ida.stubs.saml;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SamlParser {
    private final DocumentBuilder documentBuilder;
    private final UnmarshallerFactory unmarshallerFactory;

    public SamlParser() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        documentBuilder = dbf.newDocumentBuilder();
        unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
    }

    public XMLObject parseSamlString(String xmlString) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());

        try {
            Document document = documentBuilder.parse(inputStream);
            Element element = document.getDocumentElement();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (SAXException | IOException | UnmarshallingException e) {
            throw new RuntimeException(e);
        }
    }
}
