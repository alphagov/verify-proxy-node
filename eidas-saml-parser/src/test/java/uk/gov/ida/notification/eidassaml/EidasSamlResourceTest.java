package uk.gov.ida.notification.eidassaml;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.internal.util.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.slf4j.MDC;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.opensaml.utils.ObjectUtils;
import uk.gov.ida.notification.contracts.CountryMetadataResponse;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.eidassaml.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.helpers.ValidationTestDataUtils;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.MetatronProxy;
import uk.gov.ida.saml.core.test.TestCredentialFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;

@RunWith(MockitoJUnitRunner.class)
public class EidasSamlResourceTest {

    private static final String TEST_CONNECTOR_DESTINATION = "https://stub_country.acme.eu/stub-country-one/destination";

    private static final EidasAuthnRequestValidator eidasAuthnRequestValidator = mock(EidasAuthnRequestValidator.class);
    private static final MetatronProxy mockMetatronProxy = mock(MetatronProxy.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new EidasSamlResource(eidasAuthnRequestValidator, mockMetatronProxy))
            .build();

    @Before
    public void setUp() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldReturnRequestIdAndIssuer() throws Exception {
        String issuerAsString = "issuer";
        String requestId = ValidationTestDataUtils.SAMPLE_REQUEST_ID;
        String destination = ValidationTestDataUtils.SAMPLE_DESTINATION_URL;
        DateTime issueInstant = new DateTime(2015, 4, 30, 19, 25, 14, 273, DateTimeZone.forOffsetHours(0));
        Issuer issuer = ObjectUtils.createSamlObject(Issuer.class);
        issuer.setValue(issuerAsString);

        EidasAuthnRequestBuilder eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder();

        AuthnRequest eidasAuthnRequest = eidasAuthnRequestBuilder
                .withRequestId(requestId)
                .withIssuer(issuerAsString)
                .withDestination(destination)
                .withSigningCredential(new TestCredentialFactory(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential())
                .withLoa(EidasLoaEnum.LOA_SUBSTANTIAL.getUri())
                .build();

        EidasSamlParserRequest request = new EidasSamlParserRequest(Base64.encodeAsString(new SamlObjectMarshaller().transformToString(eidasAuthnRequest)));

        CountryMetadataResponse metatronResponse =
                new CountryMetadataResponse(
                        METADATA_SIGNING_A_PUBLIC_CERT,
                        TEST_RP_PUBLIC_ENCRYPTION_CERT,
                        UriBuilder.fromPath(TEST_CONNECTOR_DESTINATION).build(),
                        issuer.getValue(),
                        "EU");

        when(mockMetatronProxy.getCountryMetadata(issuer.getValue())).thenReturn(metatronResponse);

        EidasSamlParserResponse response = resources.target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(EidasSamlParserResponse.class);

        assertThat(response.getRequestId()).isEqualTo(requestId);
        assertThat(response.getIssuerEntityId()).isEqualTo(issuerAsString);
        assertThat(response.getDestination()).isEqualTo(TEST_CONNECTOR_DESTINATION);

        assertThat(MDC.get(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name())).isEqualTo(requestId);
        assertThat(MDC.get(ProxyNodeMDCKey.EIDAS_DESTINATION.name())).isEqualTo(destination);
        assertThat(MDC.get(ProxyNodeMDCKey.EIDAS_ISSUER.name())).isEqualTo(issuerAsString);
        assertThat(MDC.get(ProxyNodeMDCKey.EIDAS_ISSUE_INSTANT.name())).isEqualTo(issueInstant.toString());
        assertThat(MDC.get(ProxyNodeMDCKey.EIDAS_LOA.name())).isEqualTo(EidasLoaEnum.LOA_SUBSTANTIAL.getUri());
    }
}
