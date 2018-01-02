package uk.gov.ida.notification.apprule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HubMetadataAppRuleTests {
    @ClassRule
    public static EidasProxyNodeAppRule proxyNodeAppRule = new EidasProxyNodeAppRule();
    private SamlParser parser;

    @Before
    public void before() throws ParserConfigurationException {
        parser = new SamlParser();
    }

    @Test
    public void getLocalHubMetadata() throws Throwable {
        Response response = proxyNodeAppRule.target("/hub-metadata/local").request().get();
        String metadataXml = response.readEntity(String.class);
        EntitiesDescriptor entitiesDescriptor = parser.parseSamlString(metadataXml);
        List<String> entityIds = entitiesDescriptor.getEntityDescriptors()
                .stream()
                .map(EntityDescriptor::getEntityID)
                .collect(Collectors.toList());

        assertThat(entityIds, contains("https://dev-hub.local", "http://stub-idp-one.local/SSO/POST"));
    }

    @Test
    public void getPaasHubMetadata() throws Throwable {
        Response response = proxyNodeAppRule.target("/hub-metadata/paas").request().get();
        String metadataXml = response.readEntity(String.class);
        EntitiesDescriptor entitiesDescriptor = parser.parseSamlString(metadataXml);
        List<String> entityIds = entitiesDescriptor.getEntityDescriptors()
                .stream()
                .map(EntityDescriptor::getEntityID)
                .collect(Collectors.toList());

        assertThat(entityIds, contains("https://dev-hub.local", "http://stub-idp-one.local/SSO/POST"));
    }

    @Test
    public void getMetadataForNonexistentEnvironmentReturns404() throws Throwable {
        Response response = proxyNodeAppRule.target("/hub-metadata/missing").request().get();

        assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
    }
}
