package uk.gov.ida.notification.apprule;

import org.glassfish.jersey.internal.util.collection.ImmutableMultivaluedMap;
import org.junit.Test;
import uk.gov.ida.notification.apprule.base.ProxyNodeAppRuleTestBase;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsAppRuleTests extends ProxyNodeAppRuleTestBase {
    @Test
    public void shouldLogPrometheusMetrics() throws Exception {
        // make a request so we can count it
        Response response = proxyNodeAppRule.target("/SAML2/SSO/POST")
                .request()
                .post(Entity.form(ImmutableMultivaluedMap.empty()));
        assertThat(response.getStatus()).isEqualTo(500);

        // get the metrics
        response = proxyNodeAppRule.target("/prometheus/metrics", proxyNodeAppRule.getAdminPort())
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(200);
        final String entity = response.readEntity(String.class);
        assertThat(entity).contains("uk_gov_ida_notification_resources_EidasAuthnRequestResource_handlePostBinding_count 1.0");
    }
}
