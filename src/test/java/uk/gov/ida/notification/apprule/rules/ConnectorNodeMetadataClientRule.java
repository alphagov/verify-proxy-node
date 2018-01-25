package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;
import org.w3c.dom.Element;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.helpers.TestMetadataBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class ConnectorNodeMetadataClientRule extends DropwizardClientRule{

    public ConnectorNodeMetadataClientRule(){
        super(new TestConnectorNodeMetadataResource(getTestMetadata()));
    }

    private static Element getTestMetadata() {
        try {
            return new TestMetadataBuilder("connector_node_metadata_template.xml")
                    .withEncryptionCert(new TestKeyPair().certificate)
                    .buildElement();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create connector node's metadata for testing", e);
        }
    }

    @Path("/connector-node")
    public static class TestConnectorNodeMetadataResource {
        private Element connectorNodeMetadata;

        TestConnectorNodeMetadataResource(Element connectorNodeMetadata) {
            this.connectorNodeMetadata = connectorNodeMetadata;
        }

        @GET
        @Path("/metadata")
        public String getMetadata() throws TransformerException {
            StringWriter output = new StringWriter();

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(connectorNodeMetadata), new StreamResult(output));

            return output.toString();
        }
    }
}
