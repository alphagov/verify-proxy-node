package uk.gov.ida.notification.eidassaml.apprule;

import org.apache.http.HttpStatus;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.mockito.ArgumentCaptor;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.common.EidasConstants;
import org.slf4j.LoggerFactory;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.eidassaml.apprule.base.EidasSamlParserAppRuleTestBase;
import uk.gov.ida.notification.eidassaml.logging.EidasAuthnRequestAttributesLogger;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.helpers.X509CredentialFactory;
import uk.gov.ida.notification.saml.SamlObjectSigner;

import java.util.Map;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

public class EspAuthnRequestAppRuleTest extends EidasSamlParserAppRuleTestBase {

    private EidasAuthnRequestBuilder request;
    private SamlObjectSigner samlObjectSigner;

    @Before
    public void setup() throws Exception {
        request = new EidasAuthnRequestBuilder()
                .withIssuer(CONNECTOR_NODE_ENTITY_ID)
                .withDestination("http://proxy-node/eidasAuthnRequest");
        samlObjectSigner = new SamlObjectSigner(
                X509CredentialFactory.build(TEST_RP_PUBLIC_SIGNING_CERT, TEST_RP_PRIVATE_SIGNING_KEY)
        );
    }

    @Test
    public void shouldReturnOKForValidSignedRequest() throws Exception {
        assertGoodRequest(request);
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestNotSignedCorrectly() throws Exception {
        AuthnRequest requestWithIncorrectSigningKey = request.build();
        SamlObjectSigner samlObjectSignerIncorrectSigningKey = new SamlObjectSigner(
                X509CredentialFactory.build(STUB_COUNTRY_PUBLIC_PRIMARY_CERT, STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY)
        );
        samlObjectSignerIncorrectSigningKey.sign(requestWithIncorrectSigningKey);
        assertErrorResponseWithMessage(
                postEidasAuthnRequest(requestWithIncorrectSigningKey),
                "Error during AuthnRequest Signature Validation: SAML Validation Specification: Signature was not valid."
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestUnsigned() throws Exception {
        AuthnRequest unsignedRequest = request.withRandomRequestId().build();
        assertErrorResponseWithMessage(
                postEidasAuthnRequest(unsignedRequest),
                "Error during AuthnRequest Signature Validation: SAML Validation Specification: Message has no signature."
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestIssuerMissing() throws Exception {
        assertBadRequestWithMessage(
                request.withIssuer(""),
                "Error during AuthnRequest Signature Validation: SAML Validation Specification: SAML 'Issuer' element has no value."
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestMissingRequestId() throws Exception {
        AuthnRequest requestWithoutId = request.withoutRequestId().build();
        samlObjectSigner.sign(requestWithoutId);
        assertErrorResponseWithMessage(
                postEidasAuthnRequest(requestWithoutId),
                "Bad Authn Request from Connector Node: Missing Request ID."
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestForceAuthnFalse() throws Exception {
        assertBadRequestWithMessage(
                request.withForceAuthn(false),
                "Bad Authn Request from Connector Node: Request should require fresh authentication (forceAuthn should be true)."
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestIsPassiveTrue() throws Exception {
        assertBadRequestWithMessage(
                request.withIsPassive(true),
                "Bad Authn Request from Connector Node: Request should not require zero user interaction (isPassive should be missing or false)."
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
                "Bad Authn Request from Connector Node: SAML Validation Specification: Destination is incorrect."
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestHasIncorrectComparison() throws Exception {
        assertBadRequestWithMessage(
                request.withComparison(AuthnContextComparisonTypeEnumeration.MAXIMUM),
                "Bad Authn Request from Connector Node: Comparison type, if present, must be minimum."
        );
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestExtensionsMissing() throws Exception {
        assertBadRequestWithMessage(
                request.withoutExtensions(),
                "Bad Authn Request from Connector Node: Missing Extensions.");
    }

    @Test
    public void shouldReturnHTTP400WhenAuthnRequestHasProtocolBinding() throws Exception {
        assertBadRequestWithMessage(
                request.withProtocolBinding("protocol-binding-attribute"),
                "Bad Authn Request from Connector Node: Request should not specify protocol binding."
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
    public void authnRequestShouldHaveSupportedLOA() throws Exception {
        assertGoodRequest(request.withLoa(EidasConstants.EIDAS_LOA_SUBSTANTIAL));
        assertBadRequest(request.withLoa(EidasConstants.EIDAS_LOA_LOW));
        assertBadRequest(request.withLoa(EidasConstants.EIDAS_LOA_HIGH));
        assertBadRequest(request.withoutLoa());
    }

    @Test
    public void authnRequestShouldHaveRequestedAttributes() throws Exception {
        assertBadRequestWithMessage(
                request.withoutRequestedAttributes(),
                "Bad Authn Request from Connector Node: Missing RequestedAttributes."
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
        samlObjectSigner.sign(duplicatedRequest);
        assertGoodResponse(duplicatedRequest, postEidasAuthnRequest(duplicatedRequest));
        assertErrorResponseWithMessage(
                postEidasAuthnRequest(duplicatedRequest),
                "Bad Authn Request from Connector Node: Replay check of ID"
        );
    }

    private void assertGoodRequest(EidasAuthnRequestBuilder builder) throws Exception {
        AuthnRequest postedRequest = builder.withRandomRequestId().build();
        samlObjectSigner.sign(postedRequest);
        assertGoodResponse(postedRequest, postEidasAuthnRequest(postedRequest));
    }

    private void assertBadRequest(EidasAuthnRequestBuilder builder) throws Exception {
        AuthnRequest postedRequest = builder.withRandomRequestId().build();
        samlObjectSigner.sign(postedRequest);
        assertErrorResponse(postEidasAuthnRequest(postedRequest));
    }

    private void assertBadRequestWithMessage(EidasAuthnRequestBuilder builder, String errorMessageContains) throws Exception {
        AuthnRequest postedRequest = builder.withRandomRequestId().build();
        samlObjectSigner.sign(postedRequest);
        assertErrorResponseWithMessage(postEidasAuthnRequest(postedRequest), errorMessageContains);
    }

    private void assertGoodResponse(AuthnRequest eidasAuthnRequest, Response response) {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        EidasSamlParserResponse espResponse = response.readEntity(EidasSamlParserResponse.class);
        assertThat(espResponse.getRequestId()).isEqualTo(eidasAuthnRequest.getID());
        assertThat(espResponse.getIssuer()).isEqualTo(CONNECTOR_NODE_ENTITY_ID);
    }

    private void assertErrorResponse(Response response) {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    private void assertErrorResponseWithMessage(Response response, String errorMessageContains) {
        assertErrorResponse(response);
        assertThat(response.readEntity(String.class)).contains(errorMessageContains);
    }

    @Test
    public void shouldLogAuthnRequestAttributes() throws Exception {
        Appender<ILoggingEvent> appender = mock(Appender.class);
        Logger logger = (Logger) LoggerFactory.getLogger(EidasAuthnRequestAttributesLogger.class);
        logger.addAppender(appender);
        AuthnRequest authnRequest = request.withRequestId("request_id").build();
        samlObjectSigner.sign(authnRequest);

        postEidasAuthnRequest(authnRequest);

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender).doAppend(loggingEventArgumentCaptor.capture());

        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();
        assertEquals("request_id", mdcPropertyMap.get("eidasRequestId"));
        assertEquals("http://proxy-node/eidasAuthnRequest", mdcPropertyMap.get("eidasDestination"));
        assertEquals("2015-04-30T19:25:14.273Z", mdcPropertyMap.get("eidasIssueInstant"));
        assertEquals("http://connector-node:8080/ConnectorResponderMetadata", mdcPropertyMap.get("eidasIssuer"));
    }
}
