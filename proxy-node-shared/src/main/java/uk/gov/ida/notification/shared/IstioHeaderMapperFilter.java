package uk.gov.ida.notification.shared;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class IstioHeaderMapperFilter implements ContainerResponseFilter {

    private static final String[] ISTIO_HEADERS = {
        "x-request-id",
        "x-b3-traceid",
        "x-b3-spanid",
        "x-b3-parentspanid",
        "x-b3-sampled",
        "x-b3-flags",
        "x-ot-span-context"
    };

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        MultivaluedMap<String, String> incomingHeaders = requestContext.getHeaders();

        for (String istioHeader : ISTIO_HEADERS) {
            if (incomingHeaders.containsKey(istioHeader)) {
                responseContext.getHeaders().add(istioHeader, incomingHeaders.getFirst(istioHeader));
            }
        }
    }
}
