package uk.gov.ida.notification.helpers;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.SimpleNamespaceContext;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;

public class TestMetadataBuilder {

    private final Document metadataDocument;
    private final String SIGNING = "signing";
    private final String ENCRYPTION = "encryption";
    private final String md = "urn:oasis:names:tc:SAML:2.0:metadata";
    private final String ds = "http://www.w3.org/2000/09/xmldsig#";

    public TestMetadataBuilder(String metadataTemplateFileName) throws Exception {
        String metadataString = FileHelpers.readFileAsString(metadataTemplateFileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        metadataDocument = builder.parse(new InputSource(new StringReader(metadataString)));
    }

    public TestMetadataBuilder withEncryptionCert(String certificateString) throws XPathExpressionException, CertificateEncodingException {
        setCertificate(certificateString, ENCRYPTION);
        return this;
    }

    public TestMetadataBuilder withEncryptionCert(X509Certificate certificate) throws XPathExpressionException, CertificateEncodingException {
        String encodedCertificate = encodeCertificate(certificate);
        return withEncryptionCert(encodedCertificate);
    }

    public TestMetadataBuilder withNoEncryptionCert() throws XPathExpressionException {
        Node encryptionNode = findMetadataCertificateNode(ENCRYPTION);
        encryptionNode.getParentNode().removeChild(encryptionNode);
        return this;
    }

    public TestMetadataBuilder withSigningCert(String certificateString) throws XPathExpressionException, CertificateEncodingException {
        setCertificate(certificateString, SIGNING);
        return this;
    }

    public TestMetadataBuilder withSigningCert(X509Certificate certificate) throws XPathExpressionException, CertificateEncodingException {
        String encodedCertificate = encodeCertificate(certificate);
        return withSigningCert(encodedCertificate);
    }

    public TestMetadataBuilder withNoSigningCert() throws XPathExpressionException {
        Node signingNode = findMetadataCertificateNode(SIGNING);
        signingNode.getParentNode().removeChild(signingNode);
        return this;
    }

    public Element buildElement() {
        return metadataDocument.getDocumentElement();
    }

    public MetadataResolver buildResolver(String metadataResolverId) throws ComponentInitializationException, InitializationException {
        InitializationService.initialize();
        DOMMetadataResolver metadataResolver = new DOMMetadataResolver(metadataDocument.getDocumentElement());
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        metadataResolver.setParserPool(parserPool);
        metadataResolver.setRequireValidMetadata(true);
        metadataResolver.setId(metadataResolverId);
        metadataResolver.initialize();
        return metadataResolver;
    }

    private String encodeCertificate(X509Certificate certificate) throws CertificateEncodingException {
        return Base64.getEncoder().encodeToString(certificate.getEncoded());
    }

    private void setCertificate(String certificate, String usageType) throws XPathExpressionException {
        Node encryptionCert = findMetadataCertificateNode(usageType);
        encryptionCert.setTextContent(certificate);
    }

    private Node findMetadataCertificateNode(String usageType) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();

        HashMap<String, String> namespaceMap = new HashMap<String, String>() {{
            put("md", md);
            put("ds", ds);
        }};
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(namespaceMap);
        xPath.setNamespaceContext(namespaces);

        String xPathExpression = MessageFormat.format("//md:KeyDescriptor[@use=\"{0}\"]//ds:X509Certificate", usageType);
        return (Node) xPath.compile(xPathExpression).evaluate(metadataDocument, XPathConstants.NODE);
    }
}
