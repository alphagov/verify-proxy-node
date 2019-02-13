package uk.gov.ida.notification.shared.proxy;

import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.notification.contracts.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.VerifyServiceProviderTranslationRequest;
import uk.gov.ida.notification.contracts.VSPAuthnRequestGenerationBody;
import uk.gov.ida.notification.contracts.VSPAuthnRequestResponse;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static uk.gov.ida.notification.shared.Urls.VerifyServiceProviderUrls;

public class VerifyServiceProviderProxy {
    private final JsonClient jsonClient;
    private final URI translateHubResponseEndpoint;
    private final URI generateHubAuthnRequestEndpoint;

    public VerifyServiceProviderProxy(JsonClient jsonClient, URI vspUri) {
        this.jsonClient = jsonClient;
        this.translateHubResponseEndpoint = buildURI(vspUri, VerifyServiceProviderUrls.TRANSLATE_HUB_RESPONSE_ENDPOINT);
        this.generateHubAuthnRequestEndpoint = buildURI(vspUri, VerifyServiceProviderUrls.GENERATE_HUB_AUTHN_REQUEST_ENDPOINT);
    }

    public TranslatedHubResponse getTranslatedHubResponse(VerifyServiceProviderTranslationRequest request) {
        return jsonClient.post(request, translateHubResponseEndpoint, TranslatedHubResponse.class);
    }

    public VSPAuthnRequestResponse generateAuthnRequest() {
        VSPAuthnRequestGenerationBody request = new VSPAuthnRequestGenerationBody("LEVEL_2");
        return jsonClient.post(request, generateHubAuthnRequestEndpoint, VSPAuthnRequestResponse.class);
    }

    private URI buildURI(URI host, String path) {
        return UriBuilder.fromUri(host).path(path).build();
    }
}
