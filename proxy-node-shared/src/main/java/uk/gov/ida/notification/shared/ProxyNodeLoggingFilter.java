package uk.gov.ida.notification.shared;

import org.apache.http.HttpHeaders;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.Objects;

import static uk.gov.ida.notification.shared.IstioHeaders.X_B3_TRACEID;

@Provider
public class ProxyNodeLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String JOURNEY_ID_KEY = ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name();

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String istioTraceId = requestContext.getHeaderString(X_B3_TRACEID);
        if (istioTraceId != null && !istioTraceId.isBlank()) {
            MDC.put(X_B3_TRACEID, istioTraceId);
        }

        ProxyNodeLogger.addContext(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID, getJourneyId(requestContext));
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.REFERER, requestContext.getHeaderString(HttpHeaders.REFERER));
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.RESOURCE_PATH, getResourcePath(requestContext));
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.INGRESS_MEDIA_TYPE, requestContext.getMediaType().getType());
        ProxyNodeLogger.info("Ingress");
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EGRESS_LOCATION, responseContext.getLocation().toString());
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.RESPONSE_STATUS, String.valueOf(responseContext.getStatus()));
        ProxyNodeLogger.addContext(ProxyNodeMDCKey.EGRESS_MEDIA_TYPE, responseContext.getMediaType().getType());
        ProxyNodeLogger.info("Egress");

        final String journeyId = getJourneyId(requestContext);
        responseContext.getHeaders().add(JOURNEY_ID_KEY, journeyId);

        MDC.remove(X_B3_TRACEID);
        for (ProxyNodeMDCKey key : ProxyNodeMDCKey.values()) {
            MDC.remove(key.name());
        }
    }

    private String getResourcePath(ContainerRequestContext requestContext) {
        return requestContext.getUriInfo().getPath();
    }

    private String getJourneyId(ContainerRequestContext requestContext) {
        String journeyId = requestContext.getHeaderString(JOURNEY_ID_KEY);
        return Objects.requireNonNullElse(
                journeyId,
                MDC.get(JOURNEY_ID_KEY));
    }
}
