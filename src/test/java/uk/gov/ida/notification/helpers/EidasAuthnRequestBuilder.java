package uk.gov.ida.notification.helpers;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import uk.gov.ida.notification.saml.SamlParser;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class EidasAuthnRequestBuilder {
    private final String EIDAS_AUTHN_REQUEST_XML = "eidas_authn_request.xml";
    private final SamlParser parser;
    private final Document authnRequestDocument;
    private final String saml2 = "urn:oasis:names:tc:SAML:2.0:assertion";
    private final String saml2p = "urn:oasis:names:tc:SAML:2.0:protocol";
    private final String ds = "http://www.w3.org/2000/09/xmldsig#";
    private final String eidas = "http://eidas.europa.eu/saml-extensions";
    private HashMap<String, String> namespaceMap;

    public EidasAuthnRequestBuilder() throws Exception {
        parser = new SamlParser();
        authnRequestDocument = XmlHelpers.readDocumentFromFile(EIDAS_AUTHN_REQUEST_XML);
        namespaceMap = new HashMap<String, String>(){{
            put("saml2", saml2);
            put("saml2p", saml2p);
            put("ds", ds);
            put("eidas", eidas);
        }};
    }

    public AuthnRequest build() throws IOException, TransformerException {
        String authnRequestString = XmlHelpers.serializeDomElementToString(authnRequestDocument.getDocumentElement());
        return parser.parseSamlString(authnRequestString);
    }

    public EidasAuthnRequestBuilder withSpType(String spType) throws XPathExpressionException {
        findNode("//eidas:SPType").setTextContent(spType);
        return this;
    }

    public EidasAuthnRequestBuilder withLoa(String loa) throws XPathExpressionException {
        findNode("//saml2:AuthnContextClassRef").setTextContent(loa);
        return this;
    }

    public EidasAuthnRequestBuilder withIssuer(String issuer) throws XPathExpressionException {
        findNode("//saml2:Issuer").setTextContent(issuer);
        return this;
    }

    public EidasAuthnRequestBuilder withRequestId(String id) throws XPathExpressionException {
        findNode("//saml2p:AuthnRequest").getAttributes().getNamedItem("ID").setTextContent(id);
        return this;
    }

    public EidasAuthnRequestBuilder withRequestedAttribute(String attributeName, Map<String, String> xmlAttributes) throws XPathExpressionException {
        Element requestedAttribute = (Element) findNode(requestedAttributeXPath(attributeName));
        requestedAttribute = requestedAttribute == null
                ? (Element) createEidasRequestedAttributeNode(attributeName)
                : requestedAttribute;
        xmlAttributes.forEach(requestedAttribute::setAttribute);
        return this;
    }

    public EidasAuthnRequestBuilder withoutRequestedAttribute(String attributeName) throws XPathExpressionException {
        removeNode(requestedAttributeXPath(attributeName));
        return this;
    }

    public EidasAuthnRequestBuilder withoutIssuer() throws XPathExpressionException {
        removeNode("//saml2:Issuer");
        return this;
    }

    public EidasAuthnRequestBuilder withoutRequestId() throws XPathExpressionException {
        findNode("//saml2p:AuthnRequest").getAttributes().removeNamedItem("ID");
        return this;
    }

    public EidasAuthnRequestBuilder withoutExtensions() throws XPathExpressionException {
        removeNode("//saml2p:Extensions");
        return this;
    }

    public EidasAuthnRequestBuilder withoutSpType() throws XPathExpressionException {
        removeNode("//eidas:SPType");
        return this;
    }

    public EidasAuthnRequestBuilder withoutLoa() throws XPathExpressionException {
        removeNode("//saml2:AuthnContextClassRef");
        return this;
    }

    public EidasAuthnRequestBuilder withoutRequestedAuthnContext() throws XPathExpressionException {
        removeNode("//saml2p:RequestedAuthnContext");
        return this;
    }

    public EidasAuthnRequestBuilder withoutRequestedAttributes() throws XPathExpressionException {
        removeNode("//eidas:RequestedAttributes");
        return this;
    }

    private String requestedAttributeXPath(String attributeName) {
        return MessageFormat.format("//eidas:RequestedAttributes//eidas:RequestedAttribute[@Name=\"{0}\"]", attributeName);
    }

    private Node findNode(String xPathExpression) throws XPathExpressionException {
        return XmlHelpers.findNodeInDocument(authnRequestDocument, xPathExpression, namespaceMap);
    }

    private void removeNode(String xPathExpression) throws XPathExpressionException {
        Node node = findNode(xPathExpression);
        node.getParentNode().removeChild(node);
    }

    private Node createEidasRequestedAttributeNode(String eidasAttribute) throws XPathExpressionException {
        Node requestedAttributes = findNode("//eidas:RequestedAttributes");
        Element newRequestedAttribute = authnRequestDocument.createElement("eidas:RequestedAttribute");
        newRequestedAttribute.setAttribute("Name", eidasAttribute);
        return requestedAttributes.appendChild(newRequestedAttribute);
    }
}
