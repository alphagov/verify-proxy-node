package uk.gov.ida.notification.proxy;

import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.shared.Urls;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class TranslatorProxy {
    private final JsonClient translatorClient;
    private final URI translateHubResponsePath;

    public TranslatorProxy(JsonClient translatorClient, URI translatorURI) {
        this.translatorClient = translatorClient;
        this.translateHubResponsePath = UriBuilder.fromUri(translatorURI).path(Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH).build();
    }

    public String getTranslatedResponse(HubResponseTranslatorRequest translatorRequest) {
        return translatorClient.post(translatorRequest, translateHubResponsePath, String.class);
    }
}
