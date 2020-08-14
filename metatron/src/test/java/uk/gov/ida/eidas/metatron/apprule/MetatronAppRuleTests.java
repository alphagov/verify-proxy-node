package uk.gov.ida.eidas.metatron.apprule;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.eidas.metatron.MetatronApplication;
import uk.gov.ida.eidas.metatron.MetatronConfiguration;
import uk.gov.ida.eidas.metatron.apprule.rules.CountryMetadataClientRule;
import uk.gov.ida.eidas.metatron.apprule.rules.TestCountryMetadataResource;
import uk.gov.ida.eidas.metatron.resources.MetatronResource;
import uk.gov.ida.jerseyclient.ErrorHandlingClient;
import uk.gov.ida.jerseyclient.JsonResponseProcessor;
import uk.gov.ida.notification.apprule.rules.AppRule;
import uk.gov.ida.notification.contracts.metadata.CountryMetadataResponse;
import uk.gov.ida.notification.shared.istio.IstioHeaderStorage;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.ProxyNodeJsonClient;
import uk.gov.ida.saml.core.test.OpenSAMLRunner;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.MESSAGE_EGRESS;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.MESSAGE_INGRESS;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

@RunWith(OpenSAMLRunner.class)
public class MetatronAppRuleTests {

    @ClassRule
    public static CountryMetadataClientRule countryMetadataClientRule = new CountryMetadataClientRule(new TestCountryMetadataResource());

    @ClassRule
    public static AppRule<MetatronConfiguration> metatronAppRule = new AppRule<>(
            MetatronApplication.class,
            ConfigOverride.config("countriesConfig", countryMetadataClientRule.getTempConfigFilePath())
    );

    @ClassRule
    public static final RuleChain orderedRules = RuleChain.outerRule(countryMetadataClientRule).around(metatronAppRule);

    @Test
    public void firstValidResolvesOk() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.VALID_ONE);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        CountryMetadataResponse actual = response.readEntity(CountryMetadataResponse.class);
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        assertThat(actual.getCountryCode()).isEqualTo("VO");
        assertThat(actual.getAssertionConsumerServices()).hasSize(1);
        assertThat(actual.getAssertionConsumerServices()).anyMatch(s -> s.getLocation().toString().equals("http://foo.com/bar") && s.getIndex() == 1 && !s.isDefaultService());
        assertThat(actual.getEntityId()).isEqualTo(entityId);
        assertThatCertsMatch(actual.getSamlEncryptionCertX509(), TEST_RP_PUBLIC_ENCRYPTION_CERT);
        assertThatCertsMatch(actual.getSamlSigningCertX509(), TEST_RP_PUBLIC_SIGNING_CERT);
    }

    @Test
    public void secondValidResolvesOk() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.VALID_TWO);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        CountryMetadataResponse actual = response.readEntity(CountryMetadataResponse.class);
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        assertThat(actual.getCountryCode()).isEqualTo("VT");
        assertThat(actual.getAssertionConsumerServices()).hasSize(2);
        assertThat(actual.getAssertionConsumerServices()).anyMatch(s -> s.getLocation().toString().equals("http://foo.com/bar") && s.getIndex() == 1 && !s.isDefaultService());
        assertThat(actual.getAssertionConsumerServices()).anyMatch(s -> s.getLocation().toString().equals("http://foo.com/bar2") && s.getIndex() == 0 && s.isDefaultService());
        assertThat(actual.getEntityId()).isEqualTo(entityId);
        assertThatCertsMatch(actual.getSamlEncryptionCertX509(), TEST_RP_PUBLIC_ENCRYPTION_CERT);
        assertThatCertsMatch(actual.getSamlSigningCertX509(), TEST_RP_PUBLIC_SIGNING_CERT);
    }

    @Test
    public void expiredReturns500() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.EXPIRED);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void untrustedReturns500() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.UNTRUSTED);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void unsignedReturns500() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.UNSIGNED);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void metadataMarkedDisabledInConfigIsBadRequestFromClient() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.DISABLED);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.BAD_REQUEST);
        assertResponseIsAnErrorMap(response);
    }

    @Test
    public void metadataEndpointThrows500Error() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.THROWS_ERROR);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR);
        assertResponseIsAnErrorMap(response);
    }

    @Test
    public void entityIdNotRegistered() throws URISyntaxException {
        String entityId = getEntityId(TestCountryMetadataResource.DOES_NOT_EXIST);
        Response response = metatronAppRule.target(getUriString(entityId)).request().get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.BAD_REQUEST);
        assertResponseIsAnErrorMap(response);
    }

    @Test
    public void ingressAndEgressShouldBeLoggedByFilter() throws URISyntaxException {

        Appender<ILoggingEvent> appender = mock(Appender.class);
        Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);
        logger.addAppender(appender);

        String entityId = getEntityId(TestCountryMetadataResource.VALID_TWO);

        ProxyNodeJsonClient client = new ProxyNodeJsonClient(
                new ErrorHandlingClient(new JerseyClientBuilder(metatronAppRule.getEnvironment()).build("header-passing-client")),
                new JsonResponseProcessor(new ObjectMapper()),
                new IstioHeaderStorage());

        MDC.put(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name(), "proxy-node-journey-id");

        metatronAppRule.get(getUriString(entityId), client, String.class);

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(2)).doAppend(loggingEventArgumentCaptor.capture());

        final List<ILoggingEvent> logEvents = loggingEventArgumentCaptor.getAllValues();
        final Map<String, String> mdcPropertyMap = logEvents.stream()
                .filter(e -> e.getMessage().equals(ProxyNodeLoggingFilter.MESSAGE_INGRESS))
                .findFirst()
                .map(ILoggingEvent::getMDCPropertyMap)
                .orElseThrow();

        assertThat(mdcPropertyMap.get(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name())).isEqualTo("proxy-node-journey-id");
        assertThat(logEvents).filteredOn(e -> e.getMessage().equals(MESSAGE_INGRESS)).hasSize(1);
        assertThat(logEvents).filteredOn(e -> e.getMessage().equals(MESSAGE_EGRESS)).hasSize(1);

    }

    private String getEntityId(String name) {
        return "http://localhost:" + countryMetadataClientRule.getPort() + "/application/" + name + "/Metadata";
    }

    private String getUriString(String entityId) {
        return "http://" + UriBuilder.fromMethod(MetatronResource.class, "getCountryMetadataResponse").host("localhost").port(metatronAppRule.getLocalPort()).build(entityId).toString();
    }

    private void assertResponseIsAnErrorMap(Response response) {
        Map map = response.readEntity(Map.class);
        assertThat(map.containsKey("code")).isTrue();
        assertThat(map.containsKey("message")).isTrue();
    }

    private void assertThatCertsMatch(String actualCert, String expectedCert) {
        X509CertificateFactory factory = new X509CertificateFactory();
        X509Certificate actual = factory.createCertificate(actualCert);
        X509Certificate expected = factory.createCertificate(expectedCert);
        assertThat(actual).isEqualTo(expected);
    }
}
