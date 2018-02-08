package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.DropwizardClientRule;
import org.w3c.dom.Element;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.helpers.TestMetadataBuilder;
import uk.gov.ida.notification.helpers.XmlHelpers;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.xml.transform.TransformerException;

public class MetadataClientRule extends DropwizardClientRule{

    public MetadataClientRule(){
        super(new TestMetadataResource(getTestConnectorMetadata()));
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

    @Path("/")
    public static class TestMetadataResource {
        private Element connectorNodeMetadata;

        TestMetadataResource(Element connectorNodeMetadata) {
            this.connectorNodeMetadata = connectorNodeMetadata;
        }

        @GET
        @Path("/connector-node/metadata")
        public String getConnectorMetadata() throws TransformerException {
            return XmlHelpers.serializeDomElementToString(connectorNodeMetadata);
        }

        @GET
        @Path("/hub/metadata")
        public String getMetadata() throws TransformerException {
            return new MetadataFactory().defaultMetadata();
        }
    }
}
