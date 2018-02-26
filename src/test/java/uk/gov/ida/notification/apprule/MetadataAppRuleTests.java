package uk.gov.ida.notification.apprule;

import org.junit.Test;
import uk.gov.ida.notification.apprule.base.ProxyNodeAppRuleTestBase;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataAppRuleTests extends ProxyNodeAppRuleTestBase {
    @Test
    public void shouldConsumeMetadata() throws Exception {
        Response response = proxyNodeAppRule.target("healthcheck", proxyNodeAppRule.getAdminPort())
            .request()
            .get();

        String healthcheck = response.readEntity(String.class);

        assertThat(healthcheck).contains("\"connector-metadata\":{\"healthy\":true}");
        assertThat(healthcheck).contains("\"hub-metadata\":{\"healthy\":true}");
    }
}
