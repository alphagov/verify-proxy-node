package uk.gov.ida.notification.integration;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.ida.notification.EidasProxyNodeApplication;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.dropwizard.testing.ConfigOverride.config;
import static org.junit.Assert.assertEquals;

public class ProxyNodeIntegrationTest {
    @Rule
    public WireMockRule stubHubRule = new WireMockRule(6666);

    @ClassRule
    public static final DropwizardAppRule<EidasProxyNodeConfiguration> rule = new DropwizardAppRule<>(
            EidasProxyNodeApplication.class,
            null,
            config("hubUrl", "http://localhost:6666/stub-hub")
            );

    @Test
    public void shouldRespondAfterPostingToHub() {
        stubFor(post(urlEqualTo("/stub-hub"))
                .withRequestBody(equalTo("Hello"))
                .willReturn(aResponse().withStatus(200).withBody("Hello - Verified By Hub")));

        Client client = new JerseyClientBuilder(rule.getEnvironment()).build("test-client");
        Response response = postToProxyNode(client, "Hello");

        assertEquals(200, response.getStatus());
        assertEquals("Hello - Verified By Hub", response.readEntity(String.class));
    }

    private Response postToProxyNode(Client client, String requestBody) {
        return client.target(
                    String.format("http://localhost:%d/verify-uk", rule.getLocalPort()))
                    .request()
                    .post(Entity.text(requestBody));
    }
}
