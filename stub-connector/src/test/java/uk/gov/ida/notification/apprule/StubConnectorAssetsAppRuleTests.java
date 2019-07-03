package uk.gov.ida.notification.apprule;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import uk.gov.ida.notification.apprule.base.StubConnectorAppRuleTestBase;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class StubConnectorAssetsAppRuleTests extends StubConnectorAppRuleTestBase {

    @Test
    public void shouldReturnFavicon() throws URISyntaxException, IOException {
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("assets/favicon.ico");
        final String expectedFavicon = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        final Response response = stubConnectorAppRule.target("/favicon.ico").request().get();
        final String favicon = response.readEntity(String.class);

        assertThat(favicon).isEqualTo(expectedFavicon);
    }
}
