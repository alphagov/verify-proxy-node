package uk.gov.ida.notification.translator.apprule;

import org.glassfish.jersey.internal.util.collection.ImmutableMultivaluedMap;
import org.junit.Test;
import uk.gov.ida.notification.translator.apprule.base.TranslatorAppRuleTestBase;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsAppRuleTests extends TranslatorAppRuleTestBase {
    @Test
    public void shouldLogPrometheusMetrics() throws Exception {
        // make a request so we can count it
        Response response = translatorAppRule.target("/SAML2/SSO/Response/POST")
                .request()
                .post(Entity.form(ImmutableMultivaluedMap.empty()));
        assertThat(response.getStatus()).isEqualTo(500);

        // get the metrics
        response = translatorAppRule.target("/prometheus/metrics", translatorAppRule.getAdminPort())
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("uk_gov_ida_notification_resources_HubResponseFromGatewayResource_hubResponse_count 1.0");
    }
}
