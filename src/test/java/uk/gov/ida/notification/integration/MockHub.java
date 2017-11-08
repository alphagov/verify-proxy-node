package uk.gov.ida.notification.integration;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class MockHub extends WireMockClassRule {
    public MockHub() {
        super(wireMockConfig().dynamicPort());
    }

    public String getHubUrl() {
        return String.format("http://localhost:%d/hub", port());
    }

    public void stubVerifiedByHubResponse(String request) {
        stubFor(post(urlEqualTo("/hub"))
                .withRequestBody(equalTo(request))
                .willReturn(aResponse().withStatus(200).withBody(String.format("%s - Verified By Hub", request)))
        );
    }
}
