package uk.gov.ida.notification.shared.metadata;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.testing.junit.ResourceTestRule;
import io.dropwizard.views.ViewMessageBodyWriter;
import io.dropwizard.views.ViewRenderer;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataCertsPublishingResourceTest {

    private static final String BEGIN_LINE = "-----BEGIN CERTIFICATE-----\n";
    private static final String END_LINE = "\n-----END CERTIFICATE-----";
    private static final String METADATA_PUBLISH_PATH = "/proxy-node-md-publish-path";

    private static final String METADATA_CA_CERTS_FILE_PATH =
            MetadataCertsPublishingResourceTest.class.getClassLoader().getResource("metadata/metadataCACerts").getPath();

    private static final String METADATA_SIGNING_CERT_FILE_PATH =
            MetadataCertsPublishingResourceTest.class.getClassLoader().getResource("metadata/metadataSigningCert").getPath();

    private static final MetadataCertsPublishingResource METADATA_CERTS_PUBLISHING_RESOURCE = new MetadataCertsPublishingResource(
            URI.create(METADATA_SIGNING_CERT_FILE_PATH), URI.create(METADATA_CA_CERTS_FILE_PATH), URI.create(METADATA_PUBLISH_PATH));

    @ClassRule
    public static final ResourceTestRule metadataCertsResource = ResourceTestRule.builder()
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(new ViewMessageBodyWriter(new MetricRegistry(), ServiceLoader.load(ViewRenderer.class)))
            .addResource(METADATA_CERTS_PUBLISHING_RESOURCE)
            .build();


    @Test
    public void shouldIncludeMetadataPublishLocation() {
        final String html = metadataCertsResource.target("/").request().get().readEntity(String.class);
        assertThat(html).contains(METADATA_PUBLISH_PATH);
    }

    @Test
    public void shouldIncludeExpectedLabels() {
        final String html = metadataCertsResource.target("/").request().get().readEntity(String.class);

        assertThat(html).contains("Issuer");
        assertThat(html).contains("Validity");
        assertThat(html).contains("Not Before");
        assertThat(html).contains("Not After");
    }

    @Test
    public void shouldNotContainLeafMetadataSigningCert() throws IOException {
        final String metadataSigningCertBase64 = readCertsBase64(METADATA_SIGNING_CERT_FILE_PATH).get(0);
        final X509Certificate metadataSigningCert = generateCertificateFromBase64(metadataSigningCertBase64);

        final String html = metadataCertsResource.target("/").request().get().readEntity(String.class);

        assertThat(html).doesNotContain(metadataSigningCertBase64);
        assertThat(html).doesNotContain(getSubjectCommonName(metadataSigningCert));
    }

    @Test
    public void shouldIncludeCorrectCerts() throws IOException {
        final String metadataSigningCertBase64 = readCertsBase64(METADATA_SIGNING_CERT_FILE_PATH).get(0);
        final X509Certificate metadataSigningCert = generateCertificateFromBase64(metadataSigningCertBase64);

        final List<String> certsBase64 = readCertsBase64(METADATA_CA_CERTS_FILE_PATH);
        final List<Entry<String, X509Certificate>> certs = certsBase64.stream()
                .map(c -> new SimpleEntry<>(c, generateCertificateFromBase64(c)))
                .filter(c -> !getSubjectCommonName(c.getValue()).equals(getSubjectCommonName(metadataSigningCert)))
                .collect(Collectors.toList());

        final Response response = metadataCertsResource.target("/").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final String html = response.readEntity(String.class);
        certs.forEach(c -> {
            final String certBase64 = c.getKey().replace("\n", "&#10;").replace("=", "");
            final String commonName = getSubjectCommonName(c.getValue());

            assertThat(html).contains(certBase64);
            assertThat(html).contains(commonName);
        });
    }

    private String getSubjectCommonName(X509Certificate cert) {
        return Arrays.stream(cert.getSubjectDN().getName().split(","))
                .filter(s -> s.startsWith("CN="))
                .findFirst()
                .map(s -> s.replace("CN=", ""))
                .get();
    }

    private X509Certificate generateCertificateFromBase64(String certBase64) {
        final String certPem = BEGIN_LINE + certBase64 + END_LINE;
        try {
            return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(certPem.getBytes()));
        } catch (CertificateException e) {
            return null;
        }
    }

    private List<String> readCertsBase64(String filePath) throws IOException {
        return Arrays.stream(new String(Files.readAllBytes(Paths.get(filePath)))
                .split(BEGIN_LINE))
                .filter(s -> !s.isEmpty())
                .map(s -> s.substring(0, s.indexOf(END_LINE)))
                .collect(Collectors.toList());
    }
}
