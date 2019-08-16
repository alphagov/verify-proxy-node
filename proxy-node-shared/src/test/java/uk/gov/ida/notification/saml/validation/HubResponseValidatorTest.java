package uk.gov.ida.notification.saml.validation;

import com.google.common.collect.ImmutableList;
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
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.event.Level;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;
import uk.gov.ida.notification.helpers.HubAssertionBuilder;
import uk.gov.ida.notification.saml.deprecate.IdpResponseValidator;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.ResponseAttributesValidator;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubResponseValidatorTest {

    private static Assertion matchingDatasetAssertion;
    private static Assertion authnStatementAssertion;

    @Mock
    private static Response response;
    @Mock
    private static LoaValidator loaValidator;
    @Mock
    private static IdpResponseValidator idpResponseValidator;
    @Mock
    private static ResponseAttributesValidator responseAttributesValidator;

    @InjectMocks
    private HubResponseValidator hubResponseValidator;

    @BeforeClass
    public static void setUpClass() throws Throwable {
        InitializationService.initialize();
        VerifySamlInitializer.init();

        authnStatementAssertion = HubAssertionBuilder.anAuthnStatementAssertion().build();
        matchingDatasetAssertion = HubAssertionBuilder.aMatchingDatasetAssertion().build();
    }

    @Before
    public void setUp() {
        when(idpResponseValidator.getValidatedAssertions()).thenReturn(new ValidatedAssertions(ImmutableList.of(authnStatementAssertion, matchingDatasetAssertion)));
    }

    @Test
    public void shouldValidateIdpResponseMessage() {
        hubResponseValidator.validate(response);
        verify(idpResponseValidator, times(1)).validate(response);
    }

    @Test
    public void shouldValidateResponseAttributes() {
        hubResponseValidator.validate(response);
        verify(responseAttributesValidator, times(1)).validate(matchingDatasetAssertion.getAttributeStatements().get(0));
    }

    @Test
    public void shouldVaidateLoA() {
        hubResponseValidator.validate(response);
        verify(loaValidator, times(1)).validate(authnStatementAssertion.getAuthnStatements().get(0).getAuthnContext());
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfMatchingDatasetAssertionsNotAvailable() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Missing Matching Dataset Assertions");
        when(idpResponseValidator.getValidatedAssertions()).thenReturn(new ValidatedAssertions(ImmutableList.of(authnStatementAssertion)));

        hubResponseValidator.validate(response);
    }

    @Test
    public void shouldThrowExceptionIfAuthnStatementAssertionNotAvailable() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Missing Authn Statement Assertion");
        when(idpResponseValidator.getValidatedAssertions()).thenReturn(new ValidatedAssertions(ImmutableList.of(matchingDatasetAssertion)));

        hubResponseValidator.validate(response);
    }

    @Test
    public void shouldThrowExceptionIfEmptyAssertions() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Missing Matching Dataset Assertions");
        when(idpResponseValidator.getValidatedAssertions()).thenReturn(new ValidatedAssertions(ImmutableList.of()));

        hubResponseValidator.validate(response);

        verify(responseAttributesValidator, times(1)).validate(matchingDatasetAssertion.getAttributeStatements().get(0));
    }

    @Test
    public void shouldThrowInvalidHubExceptionIfIdpResponseInvalid() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Idp Response Error");

        doThrow(new SamlTransformationErrorException("Idp Response Error", Level.ERROR))
                .when(idpResponseValidator).validate(response);

        hubResponseValidator.validate(response);
    }

    @Test
    public void shouldThrowInvalidHubExceptionIfResponseAttributesInvalid() {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Response Attribute Error");

        doThrow(new InvalidHubResponseException("Response Attribute Error"))
                .when(responseAttributesValidator).validate(matchingDatasetAssertion.getAttributeStatements().get(0));

        hubResponseValidator.validate(response);
    }
}
