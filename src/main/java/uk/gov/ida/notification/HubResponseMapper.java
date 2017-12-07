package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.HubResponse;


public class HubResponseMapper {

    private SamlParser parser;

    public HubResponseMapper(SamlParser parser) {

        this.parser = parser;
    }

    public HubResponse map(String responseAsString) {
        String decodeStringResponse = Base64.decodeAsString(responseAsString);
        Response response = parser.parseSamlString(decodeStringResponse);
        return new HubResponse(response);
    }
}
