package uk.gov.ida.notification.proxy;

import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;

import java.net.URI;

public class TranslatorProxy {
    private final JsonClient translatorClient;
    private final URI translateHubResponsePath;

    public TranslatorProxy(JsonClient translatorClient, URI translateHubResponsePath) {
        this.translatorClient = translatorClient;
        this.translateHubResponsePath = translateHubResponsePath;
    }

    public String getTranslatedResponse(HubResponseTranslatorRequest translatorRequest) {
        return translatorClient.post(translatorRequest, translateHubResponsePath, String.class);
    }
}
