package uk.gov.ida.notification.shared;

import org.apache.http.HttpHeaders;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.util.UUID;

import static uk.gov.ida.notification.shared.IstioHeaders.X_B3_TRACEID;

@Provider
public class ProxyNodeLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private HttpServletRequest httpServletRequest;

    @Context
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

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
        responseContext.getHeaders().add(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name(), journeyId);
        httpServletRequest.getSession().setAttribute(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name(), journeyId);

        MDC.remove(X_B3_TRACEID);
        for (ProxyNodeMDCKey key : ProxyNodeMDCKey.values()) {
            MDC.remove(key.name());
        }
    }

    private String getResourcePath(ContainerRequestContext requestContext) {
        return requestContext.getUriInfo().getPath();
    }

    private String getJourneyId(ContainerRequestContext requestContext) {
        final String journeyIdKey = ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name();
        String journeyId;

        journeyId = requestContext.getHeaderString(journeyIdKey);
        if (journeyId != null) return journeyId;

        journeyId = (String) httpServletRequest.getSession().getAttribute(journeyIdKey);
        if (journeyId != null) return journeyId;

        journeyId = MDC.get(journeyIdKey);
        if (journeyId != null) return journeyId;

        return "journey-id-unknown-" + UUID.randomUUID().toString();
    }
}
