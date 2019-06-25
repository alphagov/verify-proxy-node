package uk.gov.ida.notification.shared;

import javax.inject.Singleton;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.ida.notification.shared.IstioHeaders.ISTIO_HEADERS;

@Singleton
public class IstioHeaderStorage {
    private static ThreadLocal<MultivaluedMap<String, Object>> storage;
    static {
        storage = new ThreadLocal<>();
    }

    public void captureHeaders(MultivaluedMap<String, String> requestHeaders) {
        MultivaluedMap<String, Object> istioHeaders = new MultivaluedHashMap<>();

        for (String istioHeader : ISTIO_HEADERS) {
            if (requestHeaders.containsKey(istioHeader)) {
                List<String> headers = requestHeaders.get(istioHeader);
                if(headers != null) {
                    headers.stream().forEach(v -> istioHeaders.add(istioHeader, v));
                }
            }
        }
        IstioHeaderStorage.storage.set(istioHeaders);
    }

    public void appendIstioHeadersToResponseContextHeaders(ContainerResponseContext responseContext) {
        if(storage.get() != null) {
            storage.get().forEach((k, v) -> responseContext.getHeaders().addAll(k, v));
        }
    }

    public MultivaluedMap<String, Object> getMultiMapIstioHeaders() {
        return storage.get();
    }

    public Optional<Map<String, String>> getIstioHeaders() {
        Map<String, String> firstIstioHeaders = new HashMap<>();
        if (this.getMultiMapIstioHeaders() != null) {
            this.getMultiMapIstioHeaders().forEach((k, v) -> firstIstioHeaders.put(k, v.get(0).toString()));
        }
        return Optional.ofNullable(firstIstioHeaders);
    }

    public void clear() {
        storage.remove();
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("Istio headers: ");
        this.getMultiMapIstioHeaders().forEach((k, v) -> stringBuffer.append(String.format("%s=%s |", k, v)));
        return stringBuffer.toString();
    }
}
