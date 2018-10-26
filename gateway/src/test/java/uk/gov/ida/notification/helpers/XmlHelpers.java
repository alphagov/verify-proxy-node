package uk.gov.ida.notification.helpers;

import net.shibboleth.utilities.java.support.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

public class XmlHelpers {
    public static Document readDocumentFromFile(String filename) throws IOException, SAXException, ParserConfigurationException {
        String metadataString = FileHelpers.readFileAsString(filename);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(metadataString)));
    }

    public static String serializeDomElementToString(Element element) throws TransformerException {
        StringWriter output = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(element), new StreamResult(output));
        return output.toString();
    }

    public static Node findNodeInDocument(Document document, String xPathExpression, HashMap<String, String> namespaceMap) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(namespaceMap);
        xPath.setNamespaceContext(namespaces);
        return (Node) xPath.compile(xPathExpression).evaluate(document, XPathConstants.NODE);
    }
}
