package uk.gov.ida.notification.helpers;

import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.SamlParser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class HubSamlResponseBuilder {
    private final SamlParser parser;

    public HubSamlResponseBuilder() {
        try {
            parser = new SamlParser();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public Response build() throws IOException {
        return parser.parseSamlString(FileHelpers.readFileAsString("idp_response_unencrypted.xml"));
    }
}
