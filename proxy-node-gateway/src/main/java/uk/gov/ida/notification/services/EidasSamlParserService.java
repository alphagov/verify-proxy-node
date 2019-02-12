package uk.gov.ida.notification.services;

import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.notification.dto.EidasSamlParserRequest;
import uk.gov.ida.notification.dto.EidasSamlParserResponse;
import java.net.URI;

public class EidasSamlParserService {
    private final JsonClient eidasSamlParserClient;
    private final URI eidasSamlParserURI;

    public EidasSamlParserService(
            JsonClient eidasSamlParserClient,
            URI eidasSamlParserURI) {
        this.eidasSamlParserClient = eidasSamlParserClient;
        this.eidasSamlParserURI = eidasSamlParserURI;
    }

    public EidasSamlParserResponse parse(EidasSamlParserRequest eidasSamlParserRequest) {
        return eidasSamlParserClient.post(eidasSamlParserRequest, eidasSamlParserURI, EidasSamlParserResponse.class);
    }
}
