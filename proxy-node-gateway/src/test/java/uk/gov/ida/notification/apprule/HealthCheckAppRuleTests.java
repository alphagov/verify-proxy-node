package uk.gov.ida.notification.apprule;

import org.junit.Test;
import uk.gov.ida.notification.apprule.base.GatewayAppRuleTestBase;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckAppRuleTests extends GatewayAppRuleTestBase {
    @Test
    public void shouldExposeHealthCheck() throws Exception {
        Response response = proxyNodeAppRule.target("healthcheck", proxyNodeAppRule.getAdminPort())
            .request()
            .get();

        String healthcheck = response.readEntity(String.class);

        assertThat(healthcheck).contains("\"gateway\":{\"healthy\":true}");
    }
}
