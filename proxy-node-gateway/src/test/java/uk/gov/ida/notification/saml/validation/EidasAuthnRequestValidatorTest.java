package uk.gov.ida.notification.saml.validation;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;

import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import se.litsec.eidas.opensaml.ext.SPType;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.saml.validation.components.AssertionConsumerServiceValidator;
import uk.gov.ida.notification.saml.validation.components.ComparisonValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.RequestIssuerValidator;
import uk.gov.ida.notification.saml.validation.components.RequestedAttributesValidator;
import uk.gov.ida.notification.saml.validation.components.SpTypeValidator;
import uk.gov.ida.saml.core.validators.DestinationValidator;
import uk.gov.ida.saml.hub.validators.authnrequest.DuplicateAuthnRequestValidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EidasAuthnRequestValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private EidasAuthnRequestValidator eidasAuthnRequestValidator;
    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;
    private RequestIssuerValidator requestIssuerValidator;
    private SpTypeValidator spTypeValidator;
    private LoaValidator loaValidator;
    private RequestedAttributesValidator requestedAttributesValidator;
    private DuplicateAuthnRequestValidator duplicateAuthnRequestValidator;
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
        duplicateAuthnRequestValidator = mock(DuplicateAuthnRequestValidator.class);
        comparisonValidator = mock(ComparisonValidator.class);
        destinationValidator = mock(DestinationValidator.class);
        assertionConsumerServiceValidator = mock(AssertionConsumerServiceValidator.class);
        when(duplicateAuthnRequestValidator.valid(any())).thenReturn(true);

        eidasAuthnRequestValidator = new EidasAuthnRequestValidator(requestIssuerValidator,
                                                                    spTypeValidator,
                                                                    loaValidator,
                                                                    requestedAttributesValidator,
                                                                    duplicateAuthnRequestValidator,
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
    public void shouldValidateRequestIssuerValidator() throws Throwable  {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        eidasAuthnRequestValidator.validate(request);
        verify(requestIssuerValidator, times(1)).validate(request.getIssuer());
    }

    @Test
    public void shouldValidateWithoutSpType() throws Throwable  {
        AuthnRequest request = eidasAuthnRequestBuilder.withoutSpType().build();
        eidasAuthnRequestValidator.validate(request);
        verify(spTypeValidator, times(1)).validate(null);
    }

    @Test
    public void shouldValidateWithSpType() throws Throwable  {
        AuthnRequest request = eidasAuthnRequestBuilder.withSpType(SPTypeEnumeration.PRIVATE.toString()).build();
        SPType spType = (SPType) request.getExtensions().getOrderedChildren().get(0);
        eidasAuthnRequestValidator.validate(request);
        verify(spTypeValidator, times(1)).validate(spType);
    }

    @Test
    public void shouldValidateLoA() throws Throwable  {
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
    public void shouldValidateWithoutRequestedAttributes() throws Throwable  {
        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestedAttributes().build();
        eidasAuthnRequestValidator.validate(request);
        verify(requestedAttributesValidator, times(1)).validate(null);
    }
}