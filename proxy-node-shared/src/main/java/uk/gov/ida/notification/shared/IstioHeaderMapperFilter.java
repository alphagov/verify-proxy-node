package uk.gov.ida.notification.shared;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class IstioHeaderMapperFilter implements ContainerResponseFilter, ContainerRequestFilter {

    @Inject
    private IstioHeaderStorage istioHeaderStorage;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        istioHeaderStorage.captureHeaders(requestContext.getHeaders());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        istioHeaderStorage.appendIstioHeadersToResponseContextHeaders(responseContext);
        // TODO remove once istio tracing works
        ProxyNodeLogger.info(istioHeaderStorage.toString());
        istioHeaderStorage.clear();
    }
}
