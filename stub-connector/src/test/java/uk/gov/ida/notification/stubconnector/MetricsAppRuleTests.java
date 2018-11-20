package uk.gov.ida.notification.stubconnector;

import org.junit.Test;
import uk.gov.ida.notification.stubconnector.support.StubConnectorAppRuleTestBase;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsAppRuleTests extends StubConnectorAppRuleTestBase {

    @Test
    public void shouldLogPrometheusMetrics() {
        // make a request so we can count it
        Response response = get("/Metadata");
        assertThat(response.getStatus()).isEqualTo(200);

        // get the metrics
        response = getFromAdminPort("/prometheus/metrics");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("uk_gov_ida_notification_stubconnector_resources_MetadataResource_connectorMetadata_count 1.0");
    }
}
