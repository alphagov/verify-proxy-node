package uk.gov.ida.notification.eidassaml.saml;

import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
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
import uk.gov.ida.saml.core.test.TestCredentialFactory;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;

@RunWith(MockitoJUnitRunner.class)
public class EidasAuthnRequestValidatorTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private static RequestIssuerValidator requestIssuerValidator;
    @Mock
    private static SpTypeValidator spTypeValidator;
    @Mock
    private static LoaValidator loaValidator;
    @Mock
    private static RequestedAttributesValidator requestedAttributesValidator;
    @Mock
    private static MessageReplayChecker messageReplayChecker;
    @Mock
    private static ComparisonValidator comparisonValidator;
    @Mock
    private static DestinationValidator destinationValidator;
    @Mock
    private static AssertionConsumerServiceValidator assertionConsumerServiceValidator;

    private final static Credential signingCredential = new TestCredentialFactory(METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY).getSigningCredential();

    @InjectMocks
    private EidasAuthnRequestValidator eidasAuthnRequestValidator;

    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;

    @BeforeClass
    public static void setUpClass() throws Throwable {
        InitializationService.initialize();
    }

    @Before
    public void setUp() throws Throwable {
        eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder().withRandomRequestId();
    }

    @Test
    public void shouldNotThrowExceptionIfValidAuthnRequest() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldThrowExceptionIfNullAuthnRequest() {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Null request");
        eidasAuthnRequestValidator.validate(null, signingCredential);
    }

    @Test
    public void shouldThrowExceptionIfAuthnRequestHasNoRequestId() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Request ID");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestId()
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldThrowExceptionIfAuthnRequestHasEmptyRequestId() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Request ID");

        AuthnRequest request = eidasAuthnRequestBuilder.withRequestId("")
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldThrowExceptionIfAuthnRequestHasNoExtensions() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Extensions");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutExtensions()
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldNotThrowIfAuthRequestHasIsPassiveFlagMissing() throws TransformerException, XPathExpressionException, SignatureException, MarshallingException, XMLParserException, UnmarshallingException {
        AuthnRequest request = eidasAuthnRequestBuilder
                .withoutIsPassive()
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldNotThrowIfAuthRequestHasIsPassiveFlagSetToFalse() throws TransformerException, XPathExpressionException, SignatureException, MarshallingException, XMLParserException, UnmarshallingException {
        AuthnRequest request = eidasAuthnRequestBuilder.withIsPassive(false)
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldThrowIfAuthRequestHasIsPassiveFlagSetToTrue() throws TransformerException, XPathExpressionException, SignatureException, MarshallingException, XMLParserException, UnmarshallingException {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Request should not require zero user interaction (isPassive should be missing or false)");

        AuthnRequest request = eidasAuthnRequestBuilder.withIsPassive(true)
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldNotThrowIfAuthRequestHasForceAuthSetToTrue() throws TransformerException, XPathExpressionException, SignatureException, MarshallingException, XMLParserException, UnmarshallingException {
        AuthnRequest request = eidasAuthnRequestBuilder.withForceAuthn(true)
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldNotThrowIfAuthRequestHasForceAuthSetToFalse() throws TransformerException, XPathExpressionException, SignatureException, MarshallingException, XMLParserException, UnmarshallingException {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Request should require fresh authentication (forceAuthn should be true)");

        AuthnRequest request = eidasAuthnRequestBuilder.withForceAuthn(false)
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldThrowIfAuthRequestHasForceAuthMissing() throws TransformerException, XPathExpressionException, SignatureException, MarshallingException, XMLParserException, UnmarshallingException {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Request should require fresh authentication (forceAuthn should be true)");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutForceAuthn()
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldThrowIfAuthnRequestHasProtocolBinding() throws XPathExpressionException, TransformerException, SignatureException, MarshallingException, XMLParserException, UnmarshallingException {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Request should not specify protocol binding");

        AuthnRequest request = eidasAuthnRequestBuilder.withProtocolBinding("protocol-binding-attribute")
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
    }

    @Test
    public void shouldThrowIfAuthnRequestHasUnsupportedSamlVersion() {
        final String expectedMessage = "Bad Authn Request from Connector Node: SAML Version should be " + SAMLVersion.VERSION_20.toString();

        assertThatThrownBy(() -> eidasAuthnRequestValidator.validate(eidasAuthnRequestBuilder.withSamlVersion(SAMLVersion.VERSION_10)
                .withSigningCredential(signingCredential)
                .build(), signingCredential))
                .isInstanceOf(InvalidAuthnRequestException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    public void shouldValidateRequestIssuerValidator() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
        verify(requestIssuerValidator, times(1)).validate(request.getIssuer());
    }

    @Test
    public void shouldValidateWithoutSpType() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.withoutSpType()
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
        verify(spTypeValidator, times(1)).validate(null);
    }

    @Test
    public void shouldValidateWithSpType() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.withSpType(SPTypeEnumeration.PRIVATE.toString())
                .withSigningCredential(signingCredential)
                .build();
        SPType spType = (SPType) request.getExtensions().getOrderedChildren().get(0);
        eidasAuthnRequestValidator.validate(request, signingCredential);
        verify(spTypeValidator, times(1)).validate(spType);
    }

    @Test
    public void shouldValidateLoA() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
        verify(loaValidator, times(1)).validate(request.getRequestedAuthnContext());
    }

    @Test
    public void shouldValidateRequestedAttributes() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder
                .withSigningCredential(signingCredential)
                .build();
        RequestedAttributes requestedAttributes = (RequestedAttributes) request.getExtensions().getOrderedChildren().get(1);
        eidasAuthnRequestValidator.validate(request, signingCredential);
        verify(requestedAttributesValidator, times(1)).validate(requestedAttributes);
    }

    @Test
    public void shouldValidateWithoutRequestedAttributes() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestedAttributes()
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);
        verify(requestedAttributesValidator, times(1)).validate(null);
    }

    @Test
    public void shouldValidateAssertionConsumerServiceUrl() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder
                .withSigningCredential(signingCredential)
                .build();
        eidasAuthnRequestValidator.validate(request, signingCredential);

        verify(assertionConsumerServiceValidator, times(1)).validate(request);
    }

    @Test
    public void shouldValidateComparisonAttribute() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder
                .withSigningCredential(signingCredential)
                .build();
        final RequestedAuthnContext requestedAuthnContext = request.getRequestedAuthnContext();

        eidasAuthnRequestValidator.validate(request, signingCredential);
        verify(comparisonValidator, times(1)).validate(requestedAuthnContext);
    }

    @Test
    public void shouldValidateDuplicateAuthnRequest() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder
                .withSigningCredential(signingCredential)
                .build();
        final String requestID = request.getID();

        eidasAuthnRequestValidator.validate(request, signingCredential);
        verify(messageReplayChecker, times(1)).checkReplay(requestID);
    }

    @Test
    public void shouldValidateDestination() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder
                .withSigningCredential(signingCredential)
                .build();
        final String destination = request.getDestination();

        eidasAuthnRequestValidator.validate(request, signingCredential);
        verify(destinationValidator, times(1)).validate(destination);
    }
}
