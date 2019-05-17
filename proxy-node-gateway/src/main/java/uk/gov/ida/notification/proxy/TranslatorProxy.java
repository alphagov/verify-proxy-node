package uk.gov.ida.notification.proxy;

import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.SamlFailureResponseGenerationRequest;
import uk.gov.ida.notification.exceptions.FailureResponseGenerationException;
import uk.gov.ida.notification.exceptions.TranslatorResponseException;
import uk.gov.ida.notification.shared.proxy.ProxyNodeJsonClient;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static uk.gov.ida.notification.shared.Urls.TranslatorUrls.GENERATE_FAILURE_RESPONSE_PATH;
import static uk.gov.ida.notification.shared.Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH;

public class TranslatorProxy {
    private final ProxyNodeJsonClient translatorClient;
    private final URI translatorUri;

    public TranslatorProxy(ProxyNodeJsonClient translatorClient, URI translatorUri) {
        this.translatorClient = translatorClient;
        this.translatorUri = translatorUri;
    }

    public String getTranslatedHubResponse(HubResponseTranslatorRequest translatorRequest, String sessionId) {

        final URI translateHubResponseUri = UriBuilder.fromUri(translatorUri).path(TRANSLATE_HUB_RESPONSE_PATH).build();

        try {
            return translatorClient.post(translatorRequest, translateHubResponseUri, String.class);
        } catch (ApplicationException e) {
            throw new TranslatorResponseException(e, sessionId, translatorRequest.getRequestId(), translatorRequest.getEidasRequestId());
        }
    }

    public String getSamlErrorResponse(SamlFailureResponseGenerationRequest failureResponseGenerationRequest) {

        final URI failureResponseUri = UriBuilder.fromUri(translatorUri).path(GENERATE_FAILURE_RESPONSE_PATH).build();

        try {
            return translatorClient.post(failureResponseGenerationRequest, failureResponseUri, String.class);
        } catch (Throwable e) {
            throw new FailureResponseGenerationException(e, failureResponseGenerationRequest.getEidasRequestId());
        }
    }
}
