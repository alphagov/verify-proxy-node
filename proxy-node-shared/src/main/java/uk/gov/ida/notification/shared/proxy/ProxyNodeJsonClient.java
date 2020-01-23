package uk.gov.ida.notification.shared.proxy;

import org.slf4j.MDC;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

import java.net.URI;
import java.util.Map;

public class ProxyNodeJsonClient {

    private final JsonClient jsonClient;
    private final IstioHeaderStorage istioHeaderStorage;

    public ProxyNodeJsonClient(ErrorHandlingClient errorHandlingClient, JsonResponseProcessor responseProcessor, IstioHeaderStorage istioHeaderStorage) {
        this.jsonClient = new JsonClient(errorHandlingClient, responseProcessor);
        this.istioHeaderStorage = istioHeaderStorage;
    }

    public <T> T get(URI uri, Class<T> clazz) {
        return jsonClient.get(uri, clazz, getHeaders());
    }

    public <T> T post(Object postBody, URI uri, Class<T> clazz) {
        return jsonClient.post(postBody, uri, clazz, getHeaders());
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = this.istioHeaderStorage.getIstioHeaders();
        headers.put(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name(), MDC.get(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name()));
        return headers;
    }
}
