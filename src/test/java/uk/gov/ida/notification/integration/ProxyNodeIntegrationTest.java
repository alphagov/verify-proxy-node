package uk.gov.ida.notification.integration;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class ProxyNodeIntegrationTest {
    @ClassRule
    public static final MockHub mockHub = new MockHub();

    @Rule
    public MockHub mockHubInstance = mockHub;

    @Rule
    public final ProxyNodeAppRule proxyNodeTestSupport = new ProxyNodeAppRule(mockHub);

    @Test
    public void shouldRespondAfterPostingToHub() throws InterruptedException {
        mockHubInstance.stubVerifiedByHubResponse("Hello");

        Response response = postToProxyNode("Hello");

        assertEquals(200, response.getStatus());
        assertEquals("Hello - Verified By Hub", response.readEntity(String.class));
    }

    private Response postToProxyNode(String requestBody) {
        Client client = new JerseyClientBuilder(proxyNodeTestSupport.getEnvironment())
                .withProperty("timeout", "20s")
                .withProperty("connectionTimeout", "20s")
                .build("test-client");

        return client.target(
                    String.format("http://localhost:%d/verify-uk", proxyNodeTestSupport.getLocalPort()))
                    .request()
                    .post(Entity.text(requestBody));
    }
}
