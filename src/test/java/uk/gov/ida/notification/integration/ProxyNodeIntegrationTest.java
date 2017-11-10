package uk.gov.ida.notification.integration;

import io.dropwizard.client.JerseyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class ProxyNodeIntegrationTest {
    @ClassRule
    public static final MockHub mockHub = new MockHub();

    private static final ProxyNodeTestSupport proxyNodeTestSupport = new ProxyNodeTestSupport(mockHub);

    @BeforeClass
    public static void bodger() {
        proxyNodeTestSupport.before();
    }

    @AfterClass
    public static void badger() {
        proxyNodeTestSupport.after();
    }

    @Test
    public void shouldRespondAfterPostingToHub() {
        mockHub.stubVerifiedByHubResponse("Hello");

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
