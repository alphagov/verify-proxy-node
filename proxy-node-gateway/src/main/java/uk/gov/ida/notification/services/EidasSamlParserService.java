package uk.gov.ida.notification.services;

import uk.gov.ida.notification.dto.EidasSamlParserRequest;
import uk.gov.ida.notification.dto.EidasSamlParserResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class EidasSamlParserService {
    private final Client eidasSamlParserClient;
    private final String eidasSamlParserUrl;

    public EidasSamlParserService(
            Client eidasSamlParserClient,
            String eidasSamlParserUrl) {
        this.eidasSamlParserClient = eidasSamlParserClient;
        this.eidasSamlParserUrl = eidasSamlParserUrl;
    }

    public EidasSamlParserResponse parse(EidasSamlParserRequest eidasSamlParserRequest) {

        EidasSamlParserResponse eidasSamlParserResponse = eidasSamlParserClient.target(eidasSamlParserUrl)
            .request()
            .post(Entity.entity(eidasSamlParserRequest, MediaType.APPLICATION_JSON))
            .readEntity(EidasSamlParserResponse.class);

        return eidasSamlParserResponse;
    }
}
