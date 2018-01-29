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

public class MetadataClientRule extends DropwizardClientRule{

    public MetadataClientRule(){
        super(new TestMetadataResource(getTestConnectorMetadata(), getTestHubMetadata()));
    }

    private static Element getTestConnectorMetadata() {
        try {
            return new TestMetadataBuilder("connector_node_metadata_template.xml")
                    .withEncryptionCert(new TestKeyPair().certificate)
                    .buildElement();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create connector node's metadata for testing", e);
        }
    }

    private static Element getTestHubMetadata() {
        try {
            return new TestMetadataBuilder("hub_metadata.xml")
                    .withSigningCert(new TestKeyPair().certificate)
                    .buildElement();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create hub's metadata for testing", e);
        }
    }

    @Path("/")
    public static class TestMetadataResource {
        private Element connectorNodeMetadata;
        private Element hubMetadata;

        TestMetadataResource(Element connectorNodeMetadata, Element hubMetadata) {
            this.connectorNodeMetadata = connectorNodeMetadata;
            this.hubMetadata = hubMetadata;
        }

        @GET
        @Path("/connector-node/metadata")
        public String getConnectorMetadata() throws TransformerException {
            StringWriter output = new StringWriter();

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(connectorNodeMetadata), new StreamResult(output));

            return output.toString();
        }

        @GET
        @Path("/hub/metadata")
        public String getMetadata() throws TransformerException {
            StringWriter output = new StringWriter();

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(hubMetadata), new StreamResult(output));

            return output.toString();
        }
    }
}
