package uk.gov.ida.notification.shared.logging;

import org.apache.http.HttpHeaders;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.Optional;

import static uk.gov.ida.notification.shared.istio.IstioHeaders.X_B3_TRACEID;
import static uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey.EGRESS_LOCATION;
import static uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey.EGRESS_MEDIA_TYPE;
import static uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey.INGRESS_MEDIA_TYPE;
import static uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID;
import static uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey.REFERER;
import static uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey.RESOURCE_PATH;
import static uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey.RESPONSE_STATUS;

@Provider
@IngressEgressLogging
public class ProxyNodeLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String JOURNEY_ID_KEY = PROXY_NODE_JOURNEY_ID.name();
    public static final String MESSAGE_INGRESS = "Ingress";
    public static final String MESSAGE_EGRESS = "Egress";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String istioTraceId = requestContext.getHeaderString(X_B3_TRACEID);
        if (istioTraceId != null && !istioTraceId.isBlank()) {
            MDC.put(X_B3_TRACEID, istioTraceId);
        }

        ProxyNodeLogger.addContext(PROXY_NODE_JOURNEY_ID, getJourneyId(requestContext));
        ProxyNodeLogger.addContext(REFERER, Optional.ofNullable(requestContext.getHeaderString(HttpHeaders.REFERER)).orElse(""));
        Optional.ofNullable(requestContext.getUriInfo()).ifPresent(u -> ProxyNodeLogger.addContext(RESOURCE_PATH, u.getAbsolutePath().toString()));
        Optional.ofNullable(requestContext.getMediaType()).ifPresent(m -> ProxyNodeLogger.addContext(INGRESS_MEDIA_TYPE, m.toString()));
        ProxyNodeLogger.info(MESSAGE_INGRESS);

        MDC.remove(INGRESS_MEDIA_TYPE.name());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Optional.ofNullable(responseContext.getLocation()).ifPresent(uri -> ProxyNodeLogger.addContext(EGRESS_LOCATION, uri.toString()));
        Optional.of(responseContext.getStatus()).ifPresent(code -> ProxyNodeLogger.addContext(RESPONSE_STATUS, String.valueOf(code)));
        Optional.ofNullable(responseContext.getMediaType()).ifPresent(mt -> ProxyNodeLogger.addContext(EGRESS_MEDIA_TYPE, mt.toString()));
        ProxyNodeLogger.info(MESSAGE_EGRESS);

        responseContext.getHeaders().add(JOURNEY_ID_KEY, getJourneyId(requestContext));

        MDC.remove(X_B3_TRACEID);
        for (ProxyNodeMDCKey key : ProxyNodeMDCKey.values()) {
            MDC.remove(key.name());
        }
    }

    private String getJourneyId(ContainerRequestContext requestContext) {
        return Optional.ofNullable(requestContext.getHeaderString(JOURNEY_ID_KEY))
                .orElseGet(() -> (String) requestContext.getProperty(JOURNEY_ID_KEY));
    }
}
