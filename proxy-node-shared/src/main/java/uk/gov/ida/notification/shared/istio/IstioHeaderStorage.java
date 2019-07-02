package uk.gov.ida.notification.shared.istio;

import javax.inject.Singleton;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.ida.notification.shared.istio.IstioHeaders.ISTIO_HEADERS;

@Singleton
public class IstioHeaderStorage {

    private static ThreadLocal<MultivaluedMap<String, Object>> storage = new ThreadLocal<>();

    public Map<String, String> getIstioHeaders() {
        Map<String, String> firstIstioHeaders = new HashMap<>();
        this.getMultiMapIstioHeaders().ifPresent(m ->
                m.forEach((k, v) -> firstIstioHeaders.put(k, v.get(0).toString()))
        );

        return firstIstioHeaders;
    }

    void captureHeaders(MultivaluedMap<String, String> requestHeaders) {
        MultivaluedMap<String, Object> istioHeaders = new MultivaluedHashMap<>();

        for (String istioHeader : ISTIO_HEADERS) {
            if (requestHeaders.containsKey(istioHeader)) {
                List<String> headers = requestHeaders.get(istioHeader);
                if (headers != null) {
                    headers.forEach(v -> istioHeaders.add(istioHeader, v));
                }
            }
        }

        IstioHeaderStorage.storage.set(istioHeaders);
    }

    void appendIstioHeadersToResponseContextHeaders(ContainerResponseContext responseContext) {
        this.getMultiMapIstioHeaders().ifPresent(m ->
                m.forEach((k, v) -> responseContext.getHeaders().addAll(k, v))
        );
    }

    void clear() {
        storage.remove();
    }

    private Optional<MultivaluedMap<String, Object>> getMultiMapIstioHeaders() {
        return Optional.ofNullable(storage.get());
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("Istio headers: ");
        this.getMultiMapIstioHeaders().ifPresent(m ->
                m.forEach((k, v) -> stringBuffer.append(String.format("%s=%s |", k, v)))
        );

        return stringBuffer.toString();
    }
}
