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

public class HubMetadataClientRule extends DropwizardClientRule{

    public HubMetadataClientRule(){
        super(new TestHubMetadataResource(getTestMetadata()));
    }

    private static Element getTestMetadata() {
        try {
            return new TestMetadataBuilder("hub_metadata_template.xml")
                    .withSigningCert(new TestKeyPair().certificate)
                    .buildElement();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create hub's metadata for testing", e);
        }
    }

    @Path("/hub")
    public static class TestHubMetadataResource {
        private Element hubMetadata;

        TestHubMetadataResource(Element hubMetadata) {
            this.hubMetadata = hubMetadata;
        }

        @GET
        @Path("/metadata")
        public String getMetadata() throws TransformerException {
            StringWriter output = new StringWriter();

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(hubMetadata), new StreamResult(output));

            return output.toString();
        }
    }
}
