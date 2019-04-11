package uk.gov.ida.notification.shared;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.shared.IstioHeaders.X_B3_TRACEID;

public class ProxyNodeLoggingFilterTest {

    private ProxyNodeLoggingFilter filter;

    @Before
    public void before() {
        filter = new ProxyNodeLoggingFilter();
    }

    @After
    public void after() {
        MDC.clear();
    }

    @Test
    public void should_not_add_istio_header_to_mdc_before_request_when_not_in_request_headers() throws IOException {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString(X_B3_TRACEID)).thenReturn(null);
        filter.filter(requestContext);
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        assertThat(mdcMap).isNull();
        verify(requestContext).getHeaderString(X_B3_TRACEID);
    }

    @Test
    public void should_add_istio_header_to_mdc_before_request_when_in_request_headers() throws IOException {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getHeaderString(X_B3_TRACEID)).thenReturn("foo");
        filter.filter(requestContext);
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        assertThat(mdcMap.get(X_B3_TRACEID)).isEqualTo("foo");
        verify(requestContext).getHeaderString(X_B3_TRACEID);
    }

    @Test
    public void should_only_remove_ProxyNodeMDCKeys_and_ISTO_header_after_reponse() throws IOException {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);

        MDC.put(X_B3_TRACEID,"foo");
        MDC.put(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name(),"an id");
        MDC.put("some_other_key","some value");

        filter.filter(requestContext,responseContext);
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        assertThat(mdcMap.size()).isEqualTo(1);
        assertThat(mdcMap.get("some_other_key")).isEqualTo("some value");
        verifyZeroInteractions(requestContext);
    }

}