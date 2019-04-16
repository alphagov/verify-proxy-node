package uk.gov.ida.notification.shared;

import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static uk.gov.ida.notification.shared.IstioHeaders.X_B3_TRACEID;

@Provider
public class ProxyNodeLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String istioTraceId = requestContext.getHeaderString(X_B3_TRACEID);
        if (istioTraceId != null && ! istioTraceId.isBlank()) {
            MDC.put(X_B3_TRACEID, istioTraceId);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        MDC.remove(X_B3_TRACEID);
        for (ProxyNodeMDCKey key : ProxyNodeMDCKey.values()) {
            MDC.remove(key.name());
        }
    }

}
