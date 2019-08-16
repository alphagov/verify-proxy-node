package uk.gov.ida.notification.shared.proxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProxyNodeJsonClientTest {

    @Mock
    private static ErrorHandlingClient errorHandlingClient;

    @Mock
    private static JsonResponseProcessor responseProcessor;

    @Mock
    private static IstioHeaderStorage istioHeaderStorage;

    @Spy
    @InjectMocks
    private static ProxyNodeJsonClient proxyNodeJsonClient;

    @Test
    public void shouldSendIstioHeadersInPostRequest() throws URISyntaxException {
        Map<String, String> headers = new HashMap<>();

        headers.put("header-1", "value1");
        headers.put("header-2", "value2");

        when(istioHeaderStorage.getIstioHeaders()).thenReturn(headers);

        URI uri = new URI("http://foo.bar");
        Object postBody = new Object();
        proxyNodeJsonClient.post(postBody, uri, Object.class);
        verify(errorHandlingClient).post(eq(uri), eq(headers), eq(postBody));
    }
}
