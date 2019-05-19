package uk.gov.ida.notification.shared;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.shared.IstioHeaders.X_B3_TRACEID;
import static uk.gov.ida.notification.shared.ProxyNodeLoggingFilter.JOURNEY_ID_KEY;

@RunWith(MockitoJUnitRunner.class)
public class ProxyNodeLoggingFilterTest {

    private ProxyNodeLoggingFilter filter;

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private ContainerResponseContext responseContext;

    @Mock
    private UriInfo uriInfo;

    private URI uri;

    private MultivaluedHashMap<String, Object> headers;

    @Before
    public void before() throws URISyntaxException {
        filter = new ProxyNodeLoggingFilter();
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        uri = new URI("http://www.uri.com");
        when(uriInfo.getAbsolutePath()).thenReturn(uri);
        headers = new MultivaluedHashMap<>();
    }

    @After
    public void after() {
        MDC.clear();
    }

    @Test
    public void shouldNotAddIstioHeaderToMdcBeforeRequestWhenNotInRequestHeaders() {
        when(requestContext.getHeaderString(X_B3_TRACEID)).thenReturn(null);
        when(requestContext.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        filter.filter(requestContext);
        verify(requestContext).getHeaderString(X_B3_TRACEID);
        verify(uriInfo).getAbsolutePath();
    }

    @Test
    public void shouldAddIstioHeaderToMdcBeforeRequestWhenInRequestHeaders() {
        when(requestContext.getHeaderString(X_B3_TRACEID)).thenReturn("foo");
        when(requestContext.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        filter.filter(requestContext);
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        assertThat(mdcMap.get(X_B3_TRACEID)).isEqualTo("foo");
        verify(requestContext).getHeaderString(X_B3_TRACEID);
        verify(uriInfo).getAbsolutePath();
    }

    @Test
    public void shouldOnlyRemoveProxyNodeMdcKeysAndIstioHeaderAfterResponse() {
        when(requestContext.getHeaderString(JOURNEY_ID_KEY)).thenReturn("a journey id");
        when(responseContext.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(responseContext.getLocation()).thenReturn(uri);
        when(responseContext.getHeaders()).thenReturn(headers);

        MDC.put(X_B3_TRACEID, "foo");
        MDC.put(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name(), "an id");
        MDC.put("some_other_key", "some value");

        filter.filter(requestContext, responseContext);
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        assertThat(mdcMap.size()).isEqualTo(1);
        assertThat(mdcMap.get("some_other_key")).isEqualTo("some value");
        verify(requestContext).getHeaderString(JOURNEY_ID_KEY);
        verify(requestContext, never()).getProperty(JOURNEY_ID_KEY);
    }

    @Test
    public void shouldGetJourneyIdFromRequestAttributeIfNotOnHeader() {
        when(requestContext.getHeaderString(JOURNEY_ID_KEY)).thenReturn(null);
        when(requestContext.getProperty(JOURNEY_ID_KEY)).thenReturn("a journey id");
        when(responseContext.getHeaders()).thenReturn(headers);
        filter.filter(requestContext, responseContext);
        verify(requestContext).getHeaderString(JOURNEY_ID_KEY);
        verify(requestContext).getProperty(JOURNEY_ID_KEY);
    }

    @Test
    public void shouldSetJourneyIdOnHeadersOnResponse() {
        when(requestContext.getProperty(JOURNEY_ID_KEY)).thenReturn("a journey id");
        when(responseContext.getHeaders()).thenReturn(headers);
        filter.filter(requestContext, responseContext);
        List<Object> values = headers.get(JOURNEY_ID_KEY);
        values.iterator().next();
        assertThat(values.iterator().next()).isEqualTo("a journey id");
    }
}
