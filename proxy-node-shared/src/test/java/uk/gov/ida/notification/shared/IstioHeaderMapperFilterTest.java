package uk.gov.ida.notification.shared;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.notification.shared.istio.IstioHeaderMapperFilter;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.istio.IstioHeaders;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IstioHeaderMapperFilterTest {

    @Spy
    private static IstioHeaderStorage istioHeaderStorage = new IstioHeaderStorage();

    @Mock
    private static ContainerRequestContext requestContext;

    @Mock
    private static ContainerResponseContext responseContext;

    @InjectMocks
    private static IstioHeaderMapperFilter filter;

    @Test
    public void shouldTransmitIstioHeadersInResponseContext() {
        final var requestHeaders = new MultivaluedHashMap<String, String>();
        final var responseHeaders = new MultivaluedHashMap<String, Object>();
        requestHeaders.put(IstioHeaders.X_B3_TRACEID, List.of("foo", "bar"));
        requestHeaders.putSingle(IstioHeaders.X_REQUEST_ID, "baz");
        requestHeaders.put(IstioHeaders.X_B3_SPANID, null);
        when(requestContext.getHeaders()).thenReturn(requestHeaders);
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        filter.filter(requestContext);
        filter.filter(requestContext, responseContext);

        verify(requestContext).getHeaders();
        verify(responseContext, times(2)).getHeaders();
        assertThat(responseHeaders.get(IstioHeaders.X_B3_TRACEID)).isEqualTo(List.of("foo", "bar"));
        assertThat(responseHeaders.get(IstioHeaders.X_REQUEST_ID)).isEqualTo(List.of("baz"));
        assertThat(responseHeaders.get(IstioHeaders.X_B3_SPANID)).isNull();
    }
}
