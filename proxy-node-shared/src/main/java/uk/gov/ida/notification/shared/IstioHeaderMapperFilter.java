package uk.gov.ida.notification.shared;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.ida.notification.shared.IstioHeaders.ISTIO_HEADERS;

@Provider
public class IstioHeaderMapperFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        MultivaluedMap<String, String> incomingHeaders = requestContext.getHeaders();
        List<String> valuesToLog = new ArrayList<>();
        for (String istioHeader : ISTIO_HEADERS)
            if (incomingHeaders.containsKey(istioHeader)) {
                MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();
                List<String> headerValues = incomingHeaders.get(istioHeader);
                if (headerValues != null) {
                    headerValues.stream().forEach(v -> responseHeaders.add(istioHeader, v));
                    valuesToLog.add(String.format("%s=%s", istioHeader, headerValues));
                }
            }
        // TODO remove once istio tracing works
        ProxyNodeLogger.info("Istio headers: " + valuesToLog.stream().collect(Collectors.joining("|")));
    }
}
