package uk.gov.ida.notification.proxy;

import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.exceptions.TranslatorResponseException;
import uk.gov.ida.notification.shared.Urls;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class TranslatorProxy {
    private final JsonClient translatorClient;
    private final URI translatorUri;

    public TranslatorProxy(JsonClient translatorClient, URI translatorUri) {
        this.translatorClient = translatorClient;
        this.translatorUri = translatorUri;
    }

    public String getTranslatedResponse(HubResponseTranslatorRequest translatorRequest, String sessionId) {
        try {
            return translatorClient.post(translatorRequest, translatorUri, String.class);
        } catch (ApplicationException e) {
            throw new TranslatorResponseException(e, sessionId);
        }
    }
}
