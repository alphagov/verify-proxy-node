package uk.gov.ida.notification.shared.proxy;

import org.slf4j.MDC;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

import javax.inject.Inject;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ProxyNodeJsonClient {

    private final JsonClient jsonClient;

    @Inject
    public ProxyNodeJsonClient(ErrorHandlingClient errorHandlingClient, JsonResponseProcessor responseProcessor) {
        this.jsonClient = new JsonClient(errorHandlingClient, responseProcessor);
    }

    public <T> T post(Object postBody, URI uri, Class<T> clazz) {
        return jsonClient.post(postBody, uri, clazz, getJourneyIdHeader());
    }

    private Map<String, String> getJourneyIdHeader() {
        Map<String, String> header = new HashMap<>();
        header.put(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name(), MDC.get(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name()));
        return header;
    }

}
