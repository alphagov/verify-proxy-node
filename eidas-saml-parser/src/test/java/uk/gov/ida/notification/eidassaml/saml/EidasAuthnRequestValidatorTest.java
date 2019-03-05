package uk.gov.ida.notification.eidassaml.saml;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import se.litsec.eidas.opensaml.ext.SPType;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import uk.gov.ida.notification.eidassaml.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.AssertionConsumerServiceValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.ComparisonValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.RequestedAttributesValidator;
import uk.gov.ida.notification.eidassaml.saml.validation.components.SpTypeValidator;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.saml.deprecate.DestinationValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EidasAuthnRequestValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private EidasAuthnRequestValidator eidasAuthnRequestValidator;
    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;
    private RequestIssuerValidator requestIssuerValidator;
    private SpTypeValidator spTypeValidator;
    private LoaValidator loaValidator;
    private RequestedAttributesValidator requestedAttributesValidator;
    private MessageReplayChecker messageReplayChecker;
    private ComparisonValidator comparisonValidator;
    private DestinationValidator destinationValidator;
    private AssertionConsumerServiceValidator assertionConsumerServiceValidator;

    @BeforeClass
    public static void classSetup() throws Throwable {
        InitializationService.initialize();
    }

    @Before
    public void setUp() throws Throwable {
        requestIssuerValidator = mock(RequestIssuerValidator.class);
        spTypeValidator = mock(SpTypeValidator.class);
        loaValidator = mock(LoaValidator.class);
        requestedAttributesValidator = mock(RequestedAttributesValidator.class);
        messageReplayChecker = mock(MessageReplayChecker.class);
        comparisonValidator = mock(ComparisonValidator.class);
        destinationValidator = mock(DestinationValidator.class);
        assertionConsumerServiceValidator = mock(AssertionConsumerServiceValidator.class);

        eidasAuthnRequestValidator = new EidasAuthnRequestValidator(requestIssuerValidator,
                spTypeValidator,
                loaValidator,
                requestedAttributesValidator,
                messageReplayChecker,
                comparisonValidator,
                destinationValidator,
                assertionConsumerServiceValidator);

        eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder().withRandomRequestId();
    }

    @Test
    public void shouldNotThrowExceptionIfValidAuthnRequest() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfNullAuthnRequest() {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Null request");

        eidasAuthnRequestValidator.validate(null);
    }

    @Test
    public void shouldThrowExceptionIfAuthnRequestHasNoRequestId() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Request ID");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestId().build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfAuthnRequestHasEmptyRequestId() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Request ID");

        AuthnRequest request = eidasAuthnRequestBuilder.withRequestId("").build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfAuthnRequestHasNoExtensions() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Extensions");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutExtensions().build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldNotThrowIfAuthRequestHasIsPassiveFlagMissing() throws TransformerException, XPathExpressionException {
        AuthnRequest request = eidasAuthnRequestBuilder.withoutIsPassive().build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldNotThrowIfAuthRequestHasIsPassiveFlagSetToFalse() throws TransformerException, XPathExpressionException {
        AuthnRequest request = eidasAuthnRequestBuilder.withIsPassive(false).build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowIfAuthRequestHasIsPassiveFlagSetToTrue() throws TransformerException, XPathExpressionException {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Request should not require zero user interaction (isPassive should be missing or false)");

        AuthnRequest request = eidasAuthnRequestBuilder.withIsPassive(true).build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldNotThrowIfAuthRequestHasForceAuthSetToTrue() throws TransformerException, XPathExpressionException {
        AuthnRequest request = eidasAuthnRequestBuilder.withForceAuthn(true).build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldNotThrowIfAuthRequestHasForceAuthSetToFalse() throws TransformerException, XPathExpressionException {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Request should require fresh authentication (forceAuthn should be true)");

        AuthnRequest request = eidasAuthnRequestBuilder.withForceAuthn(false).build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowIfAuthRequestHasForceAuthMissing() throws TransformerException, XPathExpressionException {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Request should require fresh authentication (forceAuthn should be true)");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutForceAuthn().build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowIfAuthnRequestHasProtocolBinding() throws XPathExpressionException, TransformerException {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Request should not specify protocol binding");

        AuthnRequest request = eidasAuthnRequestBuilder.withProtocolBinding("protocol-binding-attribute").build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test(expected = InvalidAuthnRequestException.class)
    public void shouldThrowIfAuthnRequestHasUnsupportedSamlVersion() throws XPathExpressionException, TransformerException {
        final String expectedMessage = "Bad Authn Request from Connector Node: SAML Version should be " + SAMLVersion.VERSION_20.toString();

        eidasAuthnRequestValidator.validate(eidasAuthnRequestBuilder.withSamlVersion(SAMLVersion.VERSION_10).build());
    }

    @Test
    public void shouldValidateRequestIssuerValidator() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        eidasAuthnRequestValidator.validate(request);
        verify(requestIssuerValidator, times(1)).validate(request.getIssuer());
    }

    @Test
    public void shouldValidateWithoutSpType() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.withoutSpType().build();
        eidasAuthnRequestValidator.validate(request);
        verify(spTypeValidator, times(1)).validate(null);
    }

    @Test
    public void shouldValidateWithSpType() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.withSpType(SPTypeEnumeration.PRIVATE.toString()).build();
        SPType spType = (SPType) request.getExtensions().getOrderedChildren().get(0);
        eidasAuthnRequestValidator.validate(request);
        verify(spTypeValidator, times(1)).validate(spType);
    }

    @Test
    public void shouldValidateLoA() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        eidasAuthnRequestValidator.validate(request);
        verify(loaValidator, times(1)).validate(request.getRequestedAuthnContext());
    }

    @Test
    public void shouldValidateRequestedAttributes() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        RequestedAttributes requestedAttributes = (RequestedAttributes) request.getExtensions().getOrderedChildren().get(1);
        eidasAuthnRequestValidator.validate(request);
        verify(requestedAttributesValidator, times(1)).validate(requestedAttributes);
    }

    @Test
    public void shouldValidateWithoutRequestedAttributes() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestedAttributes().build();
        eidasAuthnRequestValidator.validate(request);
        verify(requestedAttributesValidator, times(1)).validate(null);
    }

    @Test
    public void shouldValidateAssertionConsumerServiceUrl() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        eidasAuthnRequestValidator.validate(request);

        verify(assertionConsumerServiceValidator, times(1)).validate(request);
    }

    @Test
    public void shouldValidateComparisonAttribute() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        final RequestedAuthnContext requestedAuthnContext = request.getRequestedAuthnContext();

        eidasAuthnRequestValidator.validate(request);
        verify(comparisonValidator, times(1)).validate(requestedAuthnContext);
    }

    @Test
    public void shouldValidateDuplicateAuthnRequest() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        final String requestID = request.getID();

        eidasAuthnRequestValidator.validate(request);
        verify(messageReplayChecker, times(1)).checkReplay(requestID);
    }

    @Test
    public void shouldValidateDestination() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        final String destination = request.getDestination();

        eidasAuthnRequestValidator.validate(request);
        verify(destinationValidator, times(1)).validate(destination);
    }
}
