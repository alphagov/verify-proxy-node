package uk.gov.ida.notification.eidassaml.apprule;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.apprule.rules.TestMetadataResource;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.eidassaml.apprule.base.EidasSamlParserAppRuleTestBase;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.helpers.X509CredentialFactory;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.MESSAGE_EGRESS;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.MESSAGE_INGRESS;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY;

public class EspAuthnRequestAppRuleTest extends EidasSamlParserAppRuleTestBase {

    private EidasAuthnRequestBuilder request;

    @Before
    public void setUp() throws Exception {
        MDC.clear();
        request = new EidasAuthnRequestBuilder()
                .withIssuer(TestMetadataResource.CONNECTOR_ENTITY_ID)
                .withDestination("http://proxy-node/eidasAuthnRequest")
                .withAssertionConsumerServiceURL("http://assertionConsumerService.net");
    }

    @Test
    public void shouldReturnOKForValidSignedRequest() throws Exception {
        assertGoodRequest(request);
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestNotSignedCorrectly() throws Exception {
        AuthnRequest requestWithIncorrectSigningKey = request.build();
        SamlObjectSigner samlObjectSignerIncorrectSigningKey = new SamlObjectSigner(X509CredentialFactory.build(STUB_COUNTRY_PUBLIC_PRIMARY_CERT, STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY), SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, 1L);
        samlObjectSignerIncorrectSigningKey.sign(requestWithIncorrectSigningKey, "response-id");

        assertErrorResponseWithMessage(
                postEidasAuthnRequest(requestWithIncorrectSigningKey),
                "Signature cryptographic validation not successful"
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestUnsigned() throws Exception {
        AuthnRequest unsignedRequest = request.withRandomRequestId().build();

        assertErrorResponseWithMessage(
                postEidasAuthnRequest(unsignedRequest),
                "Signature cannot be null"
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestIssuerMissing() throws Exception {
        assertBadRequestWithMessage(
                request.withIssuer(""),
                "Issuer is missing"
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestMissingRequestId() throws Exception {
        AuthnRequest requestWithoutId = request.withoutRequestId().build();
        SAML_OBJECT_SIGNER.sign(requestWithoutId, null);

        assertErrorResponseWithMessage(
                postEidasAuthnRequest(requestWithoutId),
                "Bad Authn Request from Connector Node: Missing Request ID"
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestIsBlankBase64() throws Exception {
        Response response = postBlankEidasAuthnRequest();
        assertErrorResponse(response);
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestForceAuthnFalse() throws Exception {
        assertBadRequestWithMessage(
                request.withForceAuthn(false),
                "Bad Authn Request from Connector Node: Request should require fresh authentication (forceAuthn should be true)"
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestIsPassiveTrue() throws Exception {
        assertBadRequestWithMessage(
                request.withIsPassive(true),
                "Bad Authn Request from Connector Node: Request should not require zero user interaction (isPassive should be missing or false)"
        );
    }

    @Test
    public void shouldReturnOKWhenAuthnRequestIsPassiveMissing() throws Exception {
        assertGoodRequest(request.withoutIsPassive());
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestHasIncorrectDestination() throws Exception {
        assertBadRequestWithMessage(
                request.withDestination("https://bogus.eu/"),
                "Bad Authn Request from Connector Node: SAML Validation Specification: Destination is incorrect"
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestHasIncorrectComparison() throws Exception {
        assertBadRequestWithMessage(
                request.withComparison(AuthnContextComparisonTypeEnumeration.MAXIMUM),
                "Bad Authn Request from Connector Node: Comparison type, if present, must be minimum"
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestExtensionsMissing() throws Exception {
        assertBadRequestWithMessage(
                request.withoutExtensions(),
                "Bad Authn Request from Connector Node: Missing Extensions");
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestHasProtocolBinding() throws Exception {
        assertBadRequestWithMessage(
                request.withProtocolBinding("protocol-binding-attribute"),
                "Bad Authn Request from Connector Node: Request should not specify protocol binding"
        );
    }

    @Test
    public void authnRequestShouldSupportCorrectSamlVersion() throws Throwable {
        assertBadRequest(request.withSamlVersion(SAMLVersion.VERSION_10));
        assertBadRequest(request.withSamlVersion(SAMLVersion.VERSION_11));
        assertGoodRequest(request.withSamlVersion(SAMLVersion.VERSION_20));
    }

    @Test
    public void authnRequestShouldSupportSPTypePublicOrMissing() throws Exception {
        assertBadRequest(request.withSpType("private"));
        assertGoodRequest(request.withSpType("public"));
        assertGoodRequest(request.withoutSpType());
    }

    @Test
    public void authnRequestShouldSupportLoaLow() throws Exception {
        assertGoodRequest(request.withLoa(EidasConstants.EIDAS_LOA_LOW));
    }

    @Test
    public void authnRequestShouldSupportLoaSubstantial() throws Exception {
        assertGoodRequest(request.withLoa(EidasConstants.EIDAS_LOA_SUBSTANTIAL));
    }

    @Test
    public void authnRequestShouldNotSupportLoaHigh() throws Exception {
        assertBadRequest(request.withLoa(EidasConstants.EIDAS_LOA_HIGH));
    }

    @Test
    public void authnRequestShouldNotSupportMissingLoa() throws Exception {
        assertBadRequest(request.withoutLoa());
    }

    @Test
    public void authnRequestShouldHaveRequestedAttributes() throws Exception {
        assertBadRequestWithMessage(
                request.withoutRequestedAttributes(),
                "Bad Authn Request from Connector Node: Missing RequestedAttributes"
        );
    }

    @Test
    public void authnRequestShouldHaveValidAssertionConsumerServiceUrl() throws Exception {
        assertBadRequestWithMessage(
                request.withAssertionConsumerServiceURL("invalid-assertion-consumer-service-url"),
                "Bad Authn Request from Connector Node: Supplied AssertionConsumerServiceURL has no match in metadata."
        );
    }

    @Test
    public void authnRequestShouldNotBeNotDuplicated() throws Exception {
        AuthnRequest duplicatedRequest = request.build();
        SAML_OBJECT_SIGNER.sign(duplicatedRequest, "response-id");

        assertGoodResponse(duplicatedRequest, postEidasAuthnRequest(duplicatedRequest));
        assertErrorResponseWithMessage(
                postEidasAuthnRequest(duplicatedRequest),
                "Bad Authn Request from Connector Node: Replay check of ID"
        );
    }

    @Test
    public void shouldLogAuthnRequestAttributes() throws Exception {
        Appender<ILoggingEvent> appender = mock(Appender.class);
        Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);
        logger.addAppender(appender);

        AuthnRequest authnRequest = request.withRequestId("request_id").build();
        SAML_OBJECT_SIGNER.sign(authnRequest, "response-id");

        postEidasAuthnRequest(authnRequest);

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(5)).doAppend(loggingEventArgumentCaptor.capture());

        final List<ILoggingEvent> logEvents = loggingEventArgumentCaptor.getAllValues();
        final Map<String, String> mdcPropertyMap = logEvents.stream()
                .filter(e -> e.getMessage().equals(ProxyNodeLoggingFilter.MESSAGE_EGRESS))
                .findFirst()
                .map(ILoggingEvent::getMDCPropertyMap)
                .orElseThrow();

        assertThat(mdcPropertyMap.get(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name())).isEqualTo("request_id");
        assertThat(mdcPropertyMap.get(ProxyNodeMDCKey.EIDAS_DESTINATION.name())).isEqualTo("http://proxy-node/eidasAuthnRequest");
        assertThat(mdcPropertyMap.get(ProxyNodeMDCKey.EIDAS_ISSUE_INSTANT.name())).isEqualTo("2015-04-30T19:25:14.273Z");
        assertThat(mdcPropertyMap.get(ProxyNodeMDCKey.EIDAS_ISSUER.name())).isEqualTo("http://connector-node/Metadata");

        assertThat(logEvents).filteredOn(e -> e.getMessage().equals(MESSAGE_INGRESS)).hasSize(1);
        assertThat(logEvents).filteredOn(e -> e.getMessage().equals(MESSAGE_EGRESS)).hasSize(1);
    }

    private void assertGoodRequest(EidasAuthnRequestBuilder builder) throws Exception {
        AuthnRequest postedRequest = builder.withRandomRequestId().build();
        SAML_OBJECT_SIGNER.sign(postedRequest, "response-id");

        assertGoodResponse(postedRequest, postEidasAuthnRequest(postedRequest));
    }

    private void assertBadRequest(EidasAuthnRequestBuilder builder) throws Exception {
        AuthnRequest postedRequest = builder.withRandomRequestId().build();
        SAML_OBJECT_SIGNER.sign(postedRequest, "response-id");

        assertErrorResponse(postEidasAuthnRequest(postedRequest));
    }

    private void assertBadRequestWithMessage(EidasAuthnRequestBuilder builder, String errorMessageContains) throws Exception {
        AuthnRequest postedRequest = builder.withRandomRequestId().build();
        SAML_OBJECT_SIGNER.sign(postedRequest, "response-id");

        assertErrorResponseWithMessage(postEidasAuthnRequest(postedRequest), errorMessageContains);
    }

    private void assertGoodResponse(AuthnRequest eidasAuthnRequest, Response response) {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        EidasSamlParserResponse espResponse = response.readEntity(EidasSamlParserResponse.class);

        assertThat(espResponse.getRequestId()).isEqualTo(eidasAuthnRequest.getID());
        assertThat(espResponse.getIssuer()).isEqualTo(TestMetadataResource.CONNECTOR_ENTITY_ID);
    }

    private void assertErrorResponse(Response response) {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    private void assertErrorResponseWithMessage(Response response, String errorMessageContains) {
        assertErrorResponse(response);
        String entity = response.readEntity(String.class);
        assertThat(entity).contains(errorMessageContains);
    }
}
