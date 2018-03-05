package uk.gov.ida.notification.saml.validation;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.VerifySamlInitializer;
import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;
import uk.gov.ida.notification.helpers.HubResponseBuilder;
import uk.gov.ida.notification.saml.validation.components.ResponseAttributesValidator;
import uk.gov.ida.saml.hub.validators.response.idp.IdpResponseValidator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HubResponseValidatorTest {

    private HubResponseValidator hubResponseValidator;

    @Mock
    private IdpResponseValidator idpResponseValidator;
    @Mock
    private ResponseAttributesValidator responseAttributesValidator;

    private Response response;
    private Assertion matchingDatasetAssertion;
    private Assertion authnStatementAssertion;

    @BeforeClass
    public static void classSetup() throws Throwable {
        InitializationService.initialize();
        VerifySamlInitializer.init();
    }

    @Before
    public void setUp() throws Exception {
        hubResponseValidator = new HubResponseValidator(
            idpResponseValidator,
            responseAttributesValidator
        );
        authnStatementAssertion = HubResponseBuilder.anAuthnStatementAssertion().buildUnencrypted();
        matchingDatasetAssertion = HubResponseBuilder.aMatchingDatasetAssertion().buildUnencrypted();
    }

    @Test
    public void shouldValidateIdpResponseMessage() throws Exception {
        response = new HubResponseBuilder()
            .addAssertion(matchingDatasetAssertion)
            .build();

        hubResponseValidator.validate(response);
        verify(idpResponseValidator, times(1)).validate(response);
    }

    @Test
    public void shouldValidateResponseAttributes() throws Exception {
        response = new HubResponseBuilder()
            .addAssertion(authnStatementAssertion)
            .addAssertion(matchingDatasetAssertion)
            .build();

        hubResponseValidator.validate(response);
        verify(responseAttributesValidator, times(1)).validate(matchingDatasetAssertion.getAttributeStatements().get(0));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfMatchingDatasetAssertionsNotAvailable() throws Exception {
        expectedException.expect(InvalidHubResponseException.class);
        expectedException.expectMessage("Bad IDP Response from Hub: Missing Matching Dataset Assertions");

        response = new HubResponseBuilder()
            .addAssertion(authnStatementAssertion)
            .build();

        hubResponseValidator.validate(response);
        verify(responseAttributesValidator, times(1)).validate(matchingDatasetAssertion.getAttributeStatements().get(0));
    }
}
