package uk.gov.ida.notification.apprule;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckAppRuleTests extends GatewayAppRuleTestBase {

    private static final String METADATA_PUBLISH_PATH = "/proxy-node-md-publish-path";
    private static final String METADATA_CERTS_PUBLISH_PATH = "/proxy-node-md-certs-publish-path";

    private static final String METADATA_FILE_PATH =
            HealthCheckAppRuleTests.class.getClassLoader().getResource("metadata/test-proxy-node-metadata.xml").getPath();

    private static final String METADATA_CA_CERTS_FILE_PATH =
            HealthCheckAppRuleTests.class.getClassLoader().getResource("metadata/metadataCACerts").getPath();

    private static final String METADATA_SIGNING_CERT_FILE_PATH =
            HealthCheckAppRuleTests.class.getClassLoader().getResource("metadata/metadataSigningCert").getPath();

    @ClassRule
    public static final DropwizardClientRule translatorClientRule = createInitialisedClientRule(new TestTranslatorResource());

    @ClassRule
    public static final DropwizardClientRule espClientRule = createInitialisedClientRule(new TestEidasSamlResource());

    @ClassRule
    public static final DropwizardClientRule vspClientRule = createInitialisedClientRule(new TestVerifyServiceProviderResource());

    @ClassRule
    public static final GatewayAppRule proxyNodeAppRule = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("metadataPublishingConfiguration.metadataFilePath", METADATA_FILE_PATH),
            ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", METADATA_PUBLISH_PATH),
            ConfigOverride.config("metadataPublishingConfiguration.metadataSigningCertFilePath", METADATA_SIGNING_CERT_FILE_PATH),
            ConfigOverride.config("metadataPublishingConfiguration.metadataCACertsFilePath", METADATA_CA_CERTS_FILE_PATH),
            ConfigOverride.config("metadataPublishingConfiguration.metadataCertsPublishPath", METADATA_CERTS_PUBLISH_PATH),
            ConfigOverride.config("redisService.local", "true")
    );

    @ClassRule
    public static final GatewayAppRule proxyNodeAppRuleMissingMetadata = new GatewayAppRule(
            ConfigOverride.config("eidasSamlParserService.url", espClientRule.baseUri().toString()),
            ConfigOverride.config("verifyServiceProviderService.url", vspClientRule.baseUri().toString()),
            ConfigOverride.config("translatorService.url", translatorClientRule.baseUri() + "/translator/SAML2/SSO/Response"),
            ConfigOverride.config("metadataPublishingConfiguration.metadataFilePath", "metadata/invalid-md-path.xml"),
            ConfigOverride.config("metadataPublishingConfiguration.metadataPublishPath", METADATA_PUBLISH_PATH),
            ConfigOverride.config("redisService.local", "true")
    );

    @Mock
    private static Appender<ILoggingEvent> appender;

    @Captor
    private static ArgumentCaptor<ILoggingEvent> loggingEventCaptor;


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
    public void shouldPublishMetadataSigningCertificates() throws URISyntaxException, IOException, CertificateException {
        final Response response = proxyNodeAppRule.target(METADATA_CERTS_PUBLISH_PATH).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final String html = response.readEntity(String.class);
        assertThat(html).contains(METADATA_PUBLISH_PATH);
        assertThat(html).doesNotContain(getMetadataSigningCertSubjectName());
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

    private String getMetadataSigningCertSubjectName() throws IOException, CertificateException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(Paths.get(METADATA_SIGNING_CERT_FILE_PATH)));
        final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(inputStream);

        return Arrays
                .stream(cert.getSubjectDN().getName().split(","))
                .filter(s -> s.startsWith("CN="))
                .findFirst()
                .map(s -> s.replace("CN=", ""))
                .map(String::trim)
                .get();
    }
}
