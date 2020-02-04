package uk.gov.ida.notification.helpers;

import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.*;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EidasAuthnRequestBuilder {
    private final String EIDAS_AUTHN_REQUEST_XML = "eidas_authn_request.xml";
    private final SamlParser parser;
    private final Document authnRequestDocument;
    private final String saml2 = "urn:oasis:names:tc:SAML:2.0:assertion";
    private final String saml2p = "urn:oasis:names:tc:SAML:2.0:protocol";
    private final String ds = "http://www.w3.org/2000/09/xmldsig#";
    private final String eidas = "http://eidas.europa.eu/saml-extensions";
    private HashMap<String, String> namespaceMap;
    private Credential signingCredential;

    public EidasAuthnRequestBuilder() throws Exception {
        parser = new SamlParser();
        authnRequestDocument = XmlHelpers.readDocumentFromFile(EIDAS_AUTHN_REQUEST_XML);
        namespaceMap = new HashMap<>() {{
            put("saml2", saml2);
            put("saml2p", saml2p);
            put("ds", ds);
            put("eidas", eidas);
        }};
    }

    public AuthnRequest build() throws TransformerException, SignatureException, MarshallingException {
        String authnRequestString = XmlHelpers.serializeDomElementToString(authnRequestDocument.getDocumentElement());
        AuthnRequest authnRequest = parser.parseSamlString(authnRequestString);
        if (this.signingCredential != null) {
            SignatureBuilder signatureBuilder = SignatureBuilder
                    .aSignature()
                    .withSignatureAlgorithm(new SignatureRSASHA256())
                    .withSigningCredential(this.signingCredential);
            authnRequest.setSignature(signatureBuilder.build());
            XMLObjectProviderRegistrySupport
                    .getMarshallerFactory()
                    .getMarshaller(authnRequest)
                    .marshall(authnRequest);
            Signer.signObject(authnRequest.getSignature());
        }
        return authnRequest;
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

    public EidasAuthnRequestBuilder withRandomRequestId() throws XPathExpressionException {
        String randomId = "_" + UUID.randomUUID().toString().replace("-", "");
        return withRequestId(randomId);
    }

    public EidasAuthnRequestBuilder withForceAuthn(boolean forceAuthn) throws XPathExpressionException {
        findNode("//saml2p:AuthnRequest").getAttributes().getNamedItem("ForceAuthn").setNodeValue(forceAuthn ? "true" : "false");
        return this;
    }

    public EidasAuthnRequestBuilder withIsPassive(boolean isPassive) throws XPathExpressionException {
        findNode("//saml2p:AuthnRequest").getAttributes().getNamedItem("IsPassive").setNodeValue(isPassive ? "true" : "false");
        return this;
    }

    public EidasAuthnRequestBuilder withDestination(String destination) throws DOMException, XPathExpressionException {
        findNode("//saml2p:AuthnRequest").getAttributes().getNamedItem("Destination").setNodeValue(destination);
        return this;
    }

    public EidasAuthnRequestBuilder withProtocolBinding(String protocolBinding) throws DOMException, XPathExpressionException {
        createAuthnRequestAttribute("ProtocolBinding");
        findNode("//saml2p:AuthnRequest").getAttributes().getNamedItem("ProtocolBinding").setNodeValue(protocolBinding);
        return this;
    }

    public EidasAuthnRequestBuilder withSamlVersion(SAMLVersion samlVersion) throws DOMException, XPathExpressionException {
        findNode("//saml2p:AuthnRequest").getAttributes().getNamedItem("Version").setNodeValue(samlVersion.toString());
        return this;
    }

    public EidasAuthnRequestBuilder withAssertionConsumerServiceURL(String assertionConsumerServiceUrl) throws DOMException, XPathExpressionException {
        createAuthnRequestAttribute("AssertionConsumerServiceURL");
        findNode("//saml2p:AuthnRequest").getAttributes().getNamedItem("AssertionConsumerServiceURL").setNodeValue(assertionConsumerServiceUrl);
        return this;
    }

    public EidasAuthnRequestBuilder withComparison(AuthnContextComparisonTypeEnumeration comparison) throws DOMException, XPathExpressionException {
        findNode("//saml2p:RequestedAuthnContext").getAttributes().getNamedItem("Comparison").setNodeValue(comparison.toString());
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

    public EidasAuthnRequestBuilder withoutForceAuthn() throws XPathExpressionException {
        findNode("//saml2p:AuthnRequest").getAttributes().removeNamedItem("ForceAuthn");
        return this;
    }

    public EidasAuthnRequestBuilder withoutIsPassive() throws XPathExpressionException {
        findNode("//saml2p:AuthnRequest").getAttributes().removeNamedItem("IsPassive");
        return this;
    }

    public EidasAuthnRequestBuilder withoutDestination() throws XPathExpressionException {
        findNode("//saml2p:AuthnRequest").getAttributes().removeNamedItem("Destination");
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

    public EidasAuthnRequestBuilder withSigningCredential(Credential credential) {
        this.signingCredential = credential;
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

    private Node createAuthnRequestAttribute(String attributeName) throws XPathExpressionException {
        final NamedNodeMap authnRequestAttributes = findNode("//saml2p:AuthnRequest").getAttributes();
        final Attr newAttribute = authnRequestDocument.createAttribute(attributeName);
        return authnRequestAttributes.setNamedItem(newAttribute);
    }
}
