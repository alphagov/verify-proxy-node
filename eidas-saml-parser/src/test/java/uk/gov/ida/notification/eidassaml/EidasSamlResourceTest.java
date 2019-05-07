package uk.gov.ida.notification.eidassaml;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.internal.util.Base64;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.opensaml.utils.ObjectUtils;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.eidassaml.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.saml.SamlBuilder;
import uk.gov.ida.notification.shared.ProxyNodeLogger;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.builders.AuthnRequestBuilder.anAuthnRequest;

public class EidasSamlResourceTest {

    private final static String TEST_CONNECTOR_DESTINATION = "https://stub_country.acme.eu/stub-country-one/destination";

    private static EidasAuthnRequestValidator eidasAuthnRequestValidator = Mockito.mock(EidasAuthnRequestValidator.class);

    private static SamlRequestSignatureValidator samlRequestSignatureValidator = Mockito.mock(SamlRequestSignatureValidator.class);

    private static ProxyNodeLogger proxyNodeLogger = Mockito.mock(ProxyNodeLogger.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new EidasSamlResource(eidasAuthnRequestValidator, samlRequestSignatureValidator, TEST_RP_PUBLIC_ENCRYPTION_CERT, TEST_CONNECTOR_DESTINATION, proxyNodeLogger))
            .build();

    @Before
    public void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldReturnRequestIdAndIssuer() throws Exception {
        String issuerAsString = "issuer";
        String requestId = "request_id";
        String destination = "destination";
        DateTime issueInstant = new DateTime(2019, 02, 28, 9, 54);
        Issuer issuer = ObjectUtils.createSamlObject(Issuer.class);
        issuer.setValue(issuerAsString);
        AuthnRequest authnRequest = anAuthnRequest()
                .withId(requestId)
                .withIssuer(issuer)
                .withDestination(destination)
                .withIssueInstant(issueInstant)
                .build();
        setLevelOfAssurance(authnRequest, EidasLoaEnum.LOA_SUBSTANTIAL);
        EidasSamlParserRequest request = new EidasSamlParserRequest(Base64.encodeAsString(ObjectUtils.toString(authnRequest)));
        EidasSamlParserResponse response = resources.target("/eidasAuthnRequest")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(EidasSamlParserResponse.class);

        assertThat(response.getRequestId()).isEqualTo(requestId);
        assertThat(response.getIssuer()).isEqualTo(issuerAsString);
        assertThat(response.getConnectorEncryptionPublicCertificate()).isEqualTo(TEST_RP_PUBLIC_ENCRYPTION_CERT);
        assertThat(response.getDestination()).isEqualTo(TEST_CONNECTOR_DESTINATION);

        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.EIDAS_REQUEST_ID, requestId);
        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.EIDAS_DESTINATION, destination);
        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.EIDAS_ISSUER, issuerAsString);
        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.EIDAS_ISSUE_INSTANT, issueInstant.toString());
        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.EIDAS_LOA, EidasLoaEnum.LOA_SUBSTANTIAL.getUri());
    }

    private void setLevelOfAssurance(AuthnRequest authnRequest, EidasLoaEnum eidasLOA) {
        RequestedAuthnContext requestedAuthnContext = SamlBuilder.build(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
        AuthnContextClassRef authnContextClassRef = SamlBuilder.build(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(eidasLOA.getUri());
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        authnRequest.setRequestedAuthnContext(requestedAuthnContext);
    }
}
