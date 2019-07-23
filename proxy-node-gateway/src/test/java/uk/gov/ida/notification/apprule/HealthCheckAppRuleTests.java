package uk.gov.ida.notification.apprule;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.GatewayAppRule;
import uk.gov.ida.notification.apprule.rules.TestEidasSamlResource;
import uk.gov.ida.notification.apprule.rules.TestTranslatorResource;
import uk.gov.ida.notification.apprule.rules.TestVerifyServiceProviderResource;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckAppRuleTests extends GatewayAppRuleTestBase {

    private static final String METADATA_PUBLISH_PATH = "/proxy-node-md-publish-path";
    private static final String METADATA_CERTS_PUBLISH_PATH = "/proxy-node-md-certs-publish-path";

    private static final String METADATA_SIGNING_CERT_BASE64 =
            "MIIDbjCCAlagAwIBAgIJAKV9m24UYG4BMA0GCSqGSIb3DQEBCwUAMGcxCzAJBgNVBAYTAkdCMQ8wDQYDVQQHDAZMb25kb24xFzAVBgNVBAoMDkNhYmluZXQgT2ZmaWNlMQwwCgYDVQQLDANHRFMxIDAeBgNVBAMMF1ZlcmlmeSBUZXN0IE1ldGFkYXRhIENBMB4XDTE5MDcxMTE0NTgyOVoXDTE5MTAxMTE0NTgyOVowcjELMAkGA1UEBhMCR0IxDzANBgNVBAcMBkxvbmRvbjEXMBUGA1UECgwOQ2FiaW5ldCBPZmZpY2UxDDAKBgNVBAsMA0dEUzErMCkGA1UEAwwiVmVyaWZ5IFByb3h5IE5vZGUgTWV0YWRhdGEgU2lnbmluZzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKknWx3xI/b4pv4WMDOde3c4GTJ9lUNa/Gz84pKN7TPnRsRCKetqaKtBj2NGxtyy0xZKFyIOPbXXB552zHv7/w4f/9XCAVZT1QCyTtxCRoSCe1v/pq9zfB3yUGNNjTg+HzTMX91vNBtXuhgyftos8JOCgsr/1iKGYpIWnQUIHqoJP2yBGAMFW6ZY8Mtj+veWHkaVfdZLrD1J6IT5BEVbQHudZGsn1BcMQdakWPQNsI/SkMaW2dYXAvGlhDuISL6i92Q5JJKXo0aTMjuM8h9xqQbGblLh2e14c65UNe5JZJhzVmUb3kgHrzQyzSP8oWfs3L3j9r+9pD17lUXGiN1a/QMCAwEAAaMSMBAwDgYDVR0PAQH/BAQDAgeAMA0GCSqGSIb3DQEBCwUAA4IBAQBp4BOSi91FBRZSEd0BIOMXVntIT4ju8FX5VONvyOL0i80a+GRm66nbRAiXBp7wQYXyvVwJAscH6eZQ6QdL8kqhTl8XJy5d+qqk8jX8HCt3uFUfFJHmu4XMsnzbU84fq7wTZ78zpV1UxbYm4ktR6+tXqZ9cUGbn9TOWbHMrYQvheKHyHKVrVntvfLmw2sf1vqyUjKrDwtsuM8yT7dueY+mRM6mgllQrm0iK/bi8R6+KTX0trYmHaiMzdIYdPUua6Fl3vhVFB2l0q5nrBrTP/Uy3LOTGCOtZQYSfxZBSG8HfzgNQc8zNsDMQAXXll2Nyj1tT+5EeUnuqgw/kYXwu2jEZ";

    private static final String METADATA_FILE_PATH =
            HealthCheckAppRuleTests.class.getClassLoader().getResource("metadata/test-proxy-node-metadata.xml").getPath();

    private static final String METADATA_CA_CERTS_FILE_PATH =
            HealthCheckAppRuleTests.class.getClassLoader().getResource("metadata/metadataCACerts").getPath();

    @ClassRule
    public static final DropwizardClientRule translatorClientRule = new DropwizardClientRule(new TestTranslatorResource());

    @ClassRule
    public static final DropwizardClientRule espClientRule = new DropwizardClientRule(new TestEidasSamlResource());

    @ClassRule
    public static final DropwizardClientRule vspClientRule = new DropwizardClientRule(new TestVerifyServiceProviderResource());

    @Mock
    private Appender<ILoggingEvent> appender;

    @Captor
    private ArgumentCaptor<ILoggingEvent> loggingEventCaptor;

    @Rule
    public GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.local", "true"),
            ConfigOverride.config("redisService.url", ""),
            ConfigOverride.config("metadataPublishingConfiguration.metadataFilePath", METADATA_FILE_PATH),
            ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", METADATA_PUBLISH_PATH),
            ConfigOverride.config("metadataPublishingConfiguration.metadataSigningCertBase64", METADATA_SIGNING_CERT_BASE64),
            ConfigOverride.config("metadataPublishingConfiguration.metadataCertsPublishPath", METADATA_CERTS_PUBLISH_PATH),
            ConfigOverride.config("metadataPublishingConfiguration.metadataCACertsFilePath", METADATA_CA_CERTS_FILE_PATH)
    );

    @Rule
    public GatewayAppRule proxyNodeAppRuleMissingMetadata = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("redisService.local", "true"),
            ConfigOverride.config("redisService.url", ""),
            ConfigOverride.config("metadataPublishingConfiguration.metadataFilePath", "metadata/invalid-md-path.xml"),
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
        Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);
        logger.addAppender(appender);

        final String expectedMetadata = new String(Files.readAllBytes(Paths.get(METADATA_FILE_PATH)));

        final Response response = proxyNodeAppRule.target(METADATA_PUBLISH_PATH).request().get();
        final String metadata = response.readEntity(String.class);

        assertThat(response.getMediaType().toString()).isEqualTo(MediaType.APPLICATION_XML);
        assertThat(metadata).isEqualTo(expectedMetadata);

        verify(appender, never()).doAppend(loggingEventCaptor.capture());
    }

    @Test
    public void shouldReturn500WhenProxyNodeMetadataMissing() throws URISyntaxException {
        final Response response = proxyNodeAppRuleMissingMetadata.target(METADATA_PUBLISH_PATH).request().get();

        assertThat(response.getMediaType().toString()).isEqualTo(MediaType.TEXT_PLAIN);
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void shouldPublishMetadataSigningCertificates() throws URISyntaxException {
        final Response response = proxyNodeAppRule.target(METADATA_CERTS_PUBLISH_PATH).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final String html = response.readEntity(String.class);
        assertThat(html).contains(METADATA_PUBLISH_PATH);
        assertThat(html).contains("Issuer");
        assertThat(html).contains("Validity");
        assertThat(html).contains("Not Before");
        assertThat(html).contains("Not After");
    }

    @Test
    public void shouldReturnGoodResponseForFavicon() throws URISyntaxException {
        final Response response = proxyNodeAppRule.target("/favicon.ico").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.readEntity(String.class)).isEmpty();
    }
}
