package uk.gov.ida.notification.helpers;

import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.saml.SamlParser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class EidasAuthnRequestBuilder {
    private final SamlParser parser;

    public EidasAuthnRequestBuilder() {
        try {
            parser = new SamlParser();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthnRequest build() throws IOException {
        return parser.parseSamlString(FileHelpers.readFileAsString("eidas_authn_request.xml"));
    }
}
