package uk.gov.ida.notification.apprule;

import org.junit.Test;
import uk.gov.ida.notification.apprule.base.StubConnectorAppRuleTestBase;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class StubConnectorAssetsAppRuleTests extends StubConnectorAppRuleTestBase {

    @Test
    public void shouldReturnFavicon() throws IOException, URISyntaxException {
        String expectedFavicon = readResource("assets/favicon.ico");

        final Response response = stubConnectorAppRule.target("/favicon.ico").request().get();
        final String favicon = response.readEntity(String.class);

        assertThat(favicon).isEqualTo(expectedFavicon);
    }

    @Test
    public void shouldServeMetadata() throws IOException, URISyntaxException {
        final String expectedMetadata = readResource("metadata/test-stub-connector-metadata.xml");

        final Response response = stubConnectorAppRule.target(METADATA_PUBLISH_PATH).request().get();
        final String metadata = response.readEntity(String.class);

        assertThat(metadata).isEqualTo(expectedMetadata);
    }

    private String readResource(String resourcePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(
                getClass().getClassLoader().getResource(resourcePath).getPath())));
    }
}
