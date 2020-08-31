package uk.gov.ida.notification.eidassaml;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.slf4j.MDC;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import uk.gov.ida.Base64;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.metadata.AssertionConsumerService;
import uk.gov.ida.notification.contracts.metadata.CountryMetadataResponse;
import uk.gov.ida.notification.eidassaml.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.helpers.ValidationTestDataUtils;
import uk.gov.ida.notification.saml.SamlBuilder;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.MetatronProxy;
import uk.gov.ida.saml.core.test.TestCredentialFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;

@RunWith(MockitoJUnitRunner.class)
public class EidasSamlResourceTest {

    private static final String ISSUER = "issuer";
    private static final String REQUEST_ID = ValidationTestDataUtils.SAMPLE_REQUEST_ID;
    private static final DateTime ISSUE_INSTANT = new DateTime(2015, 4, 30, 19, 25, 14, 273, DateTimeZone.forOffsetHours(0));
    private static final String DESTINATION = ValidationTestDataUtils.SAMPLE_DESTINATION_URL;
    private static final String TEST_CONNECTOR_DESTINATION = "https://stub_country.acme.eu/stub-country-one/destination";

    private static final EidasAuthnRequestValidator eidasAuthnRequestValidator = mock(EidasAuthnRequestValidator.class);
    private static final MetatronProxy mockMetatronProxy = mock(MetatronProxy.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new EidasSamlResource(eidasAuthnRequestValidator, mockMetatronProxy))
            .build();

    @BeforeClass
    public static void setUp() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldReturnRequestIdAndIssuer() throws Exception {
        final AuthnRequest eidasAuthnRequest = createEidasAuthnRequestBuilder().build();
        final CountryMetadataResponse countryMetadataResponse = createCountryMetadataResponse(eidasAuthnRequest, new AssertionConsumerService(UriBuilder.fromPath(TEST_CONNECTOR_DESTINATION).build(), 0, true));

        when(mockMetatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue())).thenReturn(countryMetadataResponse);

        final EidasSamlParserResponse response = postEidasAuthnRequest(eidasAuthnRequest);

        assertThat(response.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuerEntityId()).isEqualTo(ISSUER);
        assertThat(response.getAssertionConsumerServiceLocation()).isEqualTo(TEST_CONNECTOR_DESTINATION);

        assertThat(MDC.get(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name())).isEqualTo(REQUEST_ID);
        assertThat(MDC.get(ProxyNodeMDCKey.EIDAS_DESTINATION.name())).isEqualTo(DESTINATION);
        assertThat(MDC.get(ProxyNodeMDCKey.EIDAS_ISSUER.name())).isEqualTo(ISSUER);
        assertThat(MDC.get(ProxyNodeMDCKey.EIDAS_ISSUE_INSTANT.name())).isEqualTo(ISSUE_INSTANT.toString());
        assertThat(MDC.get(ProxyNodeMDCKey.EIDAS_LOA.name())).isEqualTo(EidasLoaEnum.LOA_SUBSTANTIAL.getUri());
    }

    @Test
    public void shouldReturnFalseWhenNameIdIsPersistent() throws Exception {
        final AuthnRequest eidasAuthnRequest = createEidasAuthnRequestBuilder().build();
        NameIDPolicy nameIDPolicy = SamlBuilder.build(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIDPolicy.setFormat(NameID.PERSISTENT);
        eidasAuthnRequest.setNameIDPolicy(nameIDPolicy);

        final CountryMetadataResponse countryMetadataResponse = createCountryMetadataResponse(eidasAuthnRequest, new AssertionConsumerService(UriBuilder.fromPath(TEST_CONNECTOR_DESTINATION).build(), 0, true));

        when(mockMetatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue())).thenReturn(countryMetadataResponse);

        final EidasSamlParserResponse response = postEidasAuthnRequest(eidasAuthnRequest);

        assertThat(response.isTransientPidRequested()).isEqualTo(false);
    }

    @Test
    public void shouldReturnFalseWhenNameIdIsUnspecified() throws Exception {
        final AuthnRequest eidasAuthnRequest = createEidasAuthnRequestBuilder().build();
        NameIDPolicy nameIDPolicy = SamlBuilder.build(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIDPolicy.setFormat(NameID.UNSPECIFIED);
        eidasAuthnRequest.setNameIDPolicy(nameIDPolicy);

        final CountryMetadataResponse countryMetadataResponse = createCountryMetadataResponse(eidasAuthnRequest, new AssertionConsumerService(UriBuilder.fromPath(TEST_CONNECTOR_DESTINATION).build(), 0, true));

        when(mockMetatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue())).thenReturn(countryMetadataResponse);

        final EidasSamlParserResponse response = postEidasAuthnRequest(eidasAuthnRequest);

        assertThat(response.isTransientPidRequested()).isEqualTo(false);
    }

    @Test
    public void shouldReturnTrueWhenNameIdIsTransient() throws Exception {
        final AuthnRequest eidasAuthnRequest = createEidasAuthnRequestBuilder().build();
        NameIDPolicy nameIDPolicy = SamlBuilder.build(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIDPolicy.setFormat(NameID.TRANSIENT);
        eidasAuthnRequest.setNameIDPolicy(nameIDPolicy);

        final CountryMetadataResponse countryMetadataResponse = createCountryMetadataResponse(eidasAuthnRequest, new AssertionConsumerService(UriBuilder.fromPath(TEST_CONNECTOR_DESTINATION).build(), 0, true));

        when(mockMetatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue())).thenReturn(countryMetadataResponse);

        final EidasSamlParserResponse response = postEidasAuthnRequest(eidasAuthnRequest);

        assertThat(response.isTransientPidRequested()).isEqualTo(true);
    }

    @Test
    public void shouldReturnFalseWhenNameIdIsNull() throws Exception {
        final AuthnRequest eidasAuthnRequest = createEidasAuthnRequestBuilder().build();
        NameIDPolicy nameIDPolicy = SamlBuilder.build(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIDPolicy.setFormat(null);
        eidasAuthnRequest.setNameIDPolicy(nameIDPolicy);

        final CountryMetadataResponse countryMetadataResponse = createCountryMetadataResponse(eidasAuthnRequest, new AssertionConsumerService(UriBuilder.fromPath(TEST_CONNECTOR_DESTINATION).build(), 0, true));

        when(mockMetatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue())).thenReturn(countryMetadataResponse);

        final EidasSamlParserResponse response = postEidasAuthnRequest(eidasAuthnRequest);

        assertThat(response.isTransientPidRequested()).isEqualTo(false);
    }

    @Test
    public void shouldReturnFalseWhenNameIdPolicyIsNull() throws Exception {
        final AuthnRequest eidasAuthnRequest = createEidasAuthnRequestBuilder().build();
        eidasAuthnRequest.setNameIDPolicy(null);

        final CountryMetadataResponse countryMetadataResponse = createCountryMetadataResponse(eidasAuthnRequest, new AssertionConsumerService(UriBuilder.fromPath(TEST_CONNECTOR_DESTINATION).build(), 0, true));

        when(mockMetatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue())).thenReturn(countryMetadataResponse);

        final EidasSamlParserResponse response = postEidasAuthnRequest(eidasAuthnRequest);

        assertThat(response.isTransientPidRequested()).isEqualTo(false);
    }

    @Test
    public void shouldSelectAssertionConsumerServiceFromAuthenticationRequestIfProvided() throws Exception {
        AuthnRequest eidasAuthnRequest = createEidasAuthnRequestBuilder()
                .withAssertionConsumerServiceURL("http://www.eidas.com/Response/POST")
                .build();

        final CountryMetadataResponse countryMetadataResponse = createCountryMetadataResponse(
                eidasAuthnRequest,
                new AssertionConsumerService(URI.create("http://www.eidas.com/DefaultEndpoint/POST"), 0, true),
                new AssertionConsumerService(URI.create("http://www.eidas.com/NonDefaultEndpoint/POST"), 1, false));

        when(mockMetatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue())).thenReturn(countryMetadataResponse);

        final EidasSamlParserResponse response = postEidasAuthnRequest(eidasAuthnRequest);

        assertThat(response.getAssertionConsumerServiceLocation()).isEqualTo("http://www.eidas.com/Response/POST");
    }

    @Test
    public void shouldSelectDefaultAssertionConsumerServiceFromMetadataIfNotProvidedInAuthRequest() throws Exception {
        AuthnRequest eidasAuthnRequest = createEidasAuthnRequestBuilder().build();

        final CountryMetadataResponse countryMetadataResponse = createCountryMetadataResponse(
                eidasAuthnRequest,
                new AssertionConsumerService(URI.create("http://www.eidas.com/DefaultEndpoint/POST"), 0, true),
                new AssertionConsumerService(URI.create("http://www.eidas.com/NonDefaultEndpoint/POST"), 1, false));

        when(mockMetatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue())).thenReturn(countryMetadataResponse);

        final EidasSamlParserResponse response = postEidasAuthnRequest(eidasAuthnRequest);

        assertThat(response.getAssertionConsumerServiceLocation()).isEqualTo("http://www.eidas.com/DefaultEndpoint/POST");
    }

    private EidasSamlParserResponse postEidasAuthnRequest(AuthnRequest eidasAuthnRequest) {
        final EidasSamlParserRequest request = new EidasSamlParserRequest(Base64.encodeToString(new SamlObjectMarshaller().transformToString(eidasAuthnRequest)));
        return resources.target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(EidasSamlParserResponse.class);
    }

    private EidasAuthnRequestBuilder createEidasAuthnRequestBuilder() throws Exception {
        return new EidasAuthnRequestBuilder()
                .withRequestId(REQUEST_ID)
                .withIssuer(ISSUER)
                .withDestination(DESTINATION)
                .withSigningCredential(new TestCredentialFactory(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential())
                .withLoa(EidasLoaEnum.LOA_SUBSTANTIAL.getUri());
    }

    private CountryMetadataResponse createCountryMetadataResponse(AuthnRequest eidasAuthnRequest, AssertionConsumerService... assertionConsumerServices) {
        return new CountryMetadataResponse(
                METADATA_SIGNING_A_PUBLIC_CERT,
                TEST_RP_PUBLIC_ENCRYPTION_CERT,
                Arrays.asList(assertionConsumerServices),
                eidasAuthnRequest.getIssuer().getValue(),
                "EU");
    }
}
