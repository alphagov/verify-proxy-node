package uk.gov.ida.notification.shared.proxy;

import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VerifyServiceProviderTranslationRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestGenerationBody;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.exceptions.proxy.VspGenerateAuthnRequestResponseException;

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

    public AuthnRequestResponse generateAuthnRequest(String sessionId) {
        AuthnRequestGenerationBody request = new AuthnRequestGenerationBody(VspLevelOfAssurance.LEVEL_2.toString());
        try {
            return jsonClient.post(request, generateHubAuthnRequestEndpoint, AuthnRequestResponse.class);
        } catch (ApplicationException e) {
            throw new VspGenerateAuthnRequestResponseException(e, sessionId);
        }
    }

    private URI buildURI(URI host, String path) {
        return UriBuilder.fromUri(host).path(path).build();
    }
}