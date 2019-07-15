package uk.gov.ida.notification.eidassaml.apprule;

import org.junit.Test;
import uk.gov.ida.notification.eidassaml.apprule.base.EidasSamlParserAppRuleTestBase;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataAppRuleTests extends EidasSamlParserAppRuleTestBase {
    @Test
    public void shouldConsumeMetadata() throws Exception {
        Response response = eidasSamlParserAppRule.target("healthcheck", eidasSamlParserAppRule.getAdminPort())
                .request()
                .get();

        final String healthCheck = response.readEntity(String.class);
        assertThat(healthCheck).contains("\"connector-metadata\":{\"healthy\":true}");
    }
}
