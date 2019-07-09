package uk.gov.ida.notification.apprule;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderResource;

import javax.ws.rs.core.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckAppRuleTests extends GatewayAppRuleTestBase {

    private static final String METADATA_PUBLISH_PATH = "/proxy-node-md-publish-path";
    private static final String METADATA_RESOURCE_PATH = "metadata/test-proxy-node-metadata.xml";

    @ClassRule
    public static final DropwizardClientRule translatorClientRule = new DropwizardClientRule(new TestTranslatorResource());

    @ClassRule
    public static final DropwizardClientRule espClientRule = new DropwizardClientRule(new TestEidasSamlResource());

    @ClassRule
    public static final DropwizardClientRule vspClientRule = new DropwizardClientRule(new TestVerifyServiceProviderResource());

    @Rule
    public GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.local", "true"),
            ConfigOverride.config("redisService.url", ""),
            ConfigOverride.config("metadataPublishingConfiguration.metadataResourceFilePath", METADATA_RESOURCE_PATH),
            ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", METADATA_PUBLISH_PATH)
    );

    @Rule
    public GatewayAppRule proxyNodeAppRuleMissingMetadata = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.local", "true"),
            ConfigOverride.config("redisService.url", ""),
            ConfigOverride.config("metadataPublishingConfiguration.metadataResourceFilePath", "metadata/invalid-md-path.xml"),
            ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", METADATA_PUBLISH_PATH)
    );

    @Test
    public void shouldExposeHealthCheck() throws Exception {
        Response response = proxyNodeAppRule.target("healthcheck", proxyNodeAppRule.getAdminPort())
                .request()
                .get();

        String healthcheck = response.readEntity(String.class);

        assertThat(healthcheck).contains("\"gateway\":{\"healthy\":true}");
    }

    @Test
    public void shouldServeMetadata() throws IOException, URISyntaxException {
        final String expectedMetadata = readResource(METADATA_RESOURCE_PATH);

        final Response response = proxyNodeAppRule.target(METADATA_PUBLISH_PATH).request().get();
        final String metadata = response.readEntity(String.class);

        assertThat(metadata).isEqualTo(expectedMetadata);
    }

    @Test
    public void shouldReturnNotFoundWhenProxyNodeMetadataMissing() throws URISyntaxException {
        final Response response = proxyNodeAppRuleMissingMetadata.target(METADATA_PUBLISH_PATH).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    private String readResource(String resourcePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(
                getClass().getClassLoader().getResource(resourcePath).getPath())));
    }
}
