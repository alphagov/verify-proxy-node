package uk.gov.ida.notification.apprule;

import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.RuleChain;
import uk.gov.ida.notification.apprule.base.StubConnectorAppRuleTestBase;
import uk.gov.ida.notification.apprule.rules.StubConnectorAppRule;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class StubConnectorAssetsAppRuleTests extends StubConnectorAppRuleTestBase {

    private static final DropwizardClientRule metadataClientRule = createTestMetadataClientRule();
    private static final StubConnectorAppRule stubConnectorAppRule = createStubConnectorAppRule(metadataClientRule);

    @ClassRule
    public static final RuleChain orderedRules = RuleChain.outerRule(metadataClientRule).around(stubConnectorAppRule);

    @Test
    public void shouldReturnFavicon() throws IOException, URISyntaxException {
        String expectedFavicon = readResource("assets/favicon.ico");

        final Response response = stubConnectorAppRule.target("/favicon.ico").request().get();
        final String favicon = response.readEntity(String.class);

        assertThat(favicon).isEqualTo(expectedFavicon);
    }

    @Test
    @Ignore("ignore until /ConnectorMetadataSigningCertificates resource created")
    public void shouldPublishMetadataSigningCertificates() throws URISyntaxException {
        final Response response = stubConnectorAppRule.target(METADATA_CERTS_PUBLISH_PATH).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final String html = response.readEntity(String.class);
        assertThat(html).contains(METADATA_PUBLISH_PATH);
        assertThat(html).contains("Issuer");
        assertThat(html).contains("Validity");
        assertThat(html).contains("Not Before");
        assertThat(html).contains("Not After");
    }

    private String readResource(String resourcePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(
                getClass().getClassLoader().getResource(resourcePath).getPath())));
    }
}
