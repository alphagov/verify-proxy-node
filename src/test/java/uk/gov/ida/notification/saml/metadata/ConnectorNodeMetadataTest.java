package uk.gov.ida.notification.saml.metadata;

import io.dropwizard.testing.ResourceHelpers;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.w3c.dom.Document;
import uk.gov.ida.notification.helpers.FileHelpers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import static org.junit.Assert.assertEquals;

public class ConnectorNodeMetadataTest {

    private static final String TEST_CONNECTOR_NODE_METADATA_FILE = "connector_node_metadata.xml";

    @Test
    public void shouldReturnConnectorNodeEncryptionCertificateFromRemoteMetadata() throws Exception {
        String connectorNodeMetadataEntityId = "http://connector-node:8080/ConnectorResponderMetadata";
        PublicKey expectedPublicKey = readEncryptionPublicKeyFrom(TEST_CONNECTOR_NODE_METADATA_FILE);

        InitializationService.initialize();
        String testConnectorNodeMetadataFilepath = ResourceHelpers.resourceFilePath(TEST_CONNECTOR_NODE_METADATA_FILE);
        FilesystemMetadataResolver metadataResolver = initializeMetadataResolver(testConnectorNodeMetadataFilepath);

        ConnectorNodeMetadata connectorNodeMetadata = new ConnectorNodeMetadata(metadataResolver, connectorNodeMetadataEntityId);
        PublicKey connectorNodeEncryptionCert = connectorNodeMetadata.getEncryptionCertificate();

        assertEquals(expectedPublicKey, connectorNodeEncryptionCert);
    }

    private FilesystemMetadataResolver initializeMetadataResolver(String testConnectorNodeMetadataFilepath) throws ResolverException, ComponentInitializationException {
        FilesystemMetadataResolver metadataResolver = new FilesystemMetadataResolver(new File(testConnectorNodeMetadataFilepath));
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        metadataResolver.setParserPool(parserPool);
        metadataResolver.setRequireValidMetadata(true);
        metadataResolver.setId("someId");
        metadataResolver.initialize();
        return metadataResolver;
    }

    private PublicKey readEncryptionPublicKeyFrom(String file) throws Exception {
        String metadataString = FileHelpers.readFileAsString(file);

        InputStream metadataXml = new ByteArrayInputStream(metadataString.getBytes());
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(metadataXml);

        XPath xpath = XPathFactory.newInstance().newXPath();
        String x509CertificateString = xpath.evaluate("//KeyDescriptor[@use='encryption']//X509Certificate/text()", doc).replaceAll("\n\\s*", "");
        String pemString = "-----BEGIN CERTIFICATE-----\n" + x509CertificateString + "\n-----END CERTIFICATE-----";

        InputStream certificateInputStream = new ByteArrayInputStream(pemString.getBytes());
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        Certificate certificate = certFactory.generateCertificate(certificateInputStream);
        return certificate.getPublicKey();
    }
}