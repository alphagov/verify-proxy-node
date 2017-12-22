package uk.gov.ida.notification.apprule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.security.credential.UsageType;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.isEqual;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ConnectorNodeMetadataAppRuleTests {
    @ClassRule
    public static EidasProxyNodeAppRule proxyNodeAppRule = new EidasProxyNodeAppRule();
    private SamlParser parser;

    @Before
    public void before() throws ParserConfigurationException {
        parser = new SamlParser();
    }

    @Test
    public void getLocalConnectorNodeMetadata() throws Throwable {
        Response response = proxyNodeAppRule.target("/connector-node-metadata/local").request().get();
        String metadataXml = response.readEntity(String.class);

        EntityDescriptor entityDescriptor = parser.parseSamlString(metadataXml);

        RoleDescriptor roleDescriptor = entityDescriptor.getRoleDescriptors().get(0);
        List<String> signInLocations = roleDescriptor.getEndpoints()
                .stream()
                .map(Endpoint::getLocation)
                .collect(Collectors.toList());
        List<UsageType> keyUsages = roleDescriptor.getKeyDescriptors()
                .stream()
                .map(KeyDescriptor::getUse)
                .collect(Collectors.toList());

        assertThat(keyUsages, containsInAnyOrder(UsageType.ENCRYPTION, UsageType.SIGNING));
        assertThat(signInLocations, containsInAnyOrder("http://localhost:6600/SAML2/SSO/POST", "http://localhost:6600/SAML2/SSO/Redirect"));
    }

    @Test
    public void getPaasConnectorNodeMetadata() throws Throwable {
        Response response = proxyNodeAppRule.target("/connector-node-metadata/paas").request().get();
        String metadataXml = response.readEntity(String.class);

        EntityDescriptor entityDescriptor = parser.parseSamlString(metadataXml);

        RoleDescriptor roleDescriptor = entityDescriptor.getRoleDescriptors().get(0);
        List<String> signInLocations = roleDescriptor.getEndpoints()
                .stream()
                .map(Endpoint::getLocation)
                .collect(Collectors.toList());
        List<UsageType> keyUsages = roleDescriptor.getKeyDescriptors()
                .stream()
                .map(KeyDescriptor::getUse)
                .collect(Collectors.toList());

        assertThat(keyUsages, containsInAnyOrder(UsageType.ENCRYPTION, UsageType.SIGNING));
        assertThat(signInLocations, containsInAnyOrder("https://verify-eidas-notification.cloudapps.digital/SAML2/SSO/POST", "https://verify-eidas-notification.cloudapps.digital/SAML2/SSO/Redirect"));
    }

    @Test
    public void getMetadataForNonexistentEnvironmentReturns404() throws Throwable {
        Response response = proxyNodeAppRule.target("/connector-node-metadata/missing").request().get();

        assertEquals(NOT_FOUND.getStatusCode(), response.getStatus());
    }
}
