package uk.gov.ida.notification.saml.deprecate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IdpResponseValidatorTest {

    private IdpResponseValidator responseValidator;

    @Mock
    private SamlResponseSignatureValidator samlResponseSignatureValidator;
    @Mock
    private AssertionDecrypter assertionDecrypter;
    @Mock
    private SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;
    @Mock
    private EncryptedResponseFromIdpValidator responseFromIdpValidator;
    @Mock
    private DestinationValidator responseDestinationValidator;
    @Mock
    private ResponseAssertionsFromIdpValidator responseAssertionsFromIdpValidator;

    @Before
    public void setUp() {
        responseValidator = new IdpResponseValidator(
                samlResponseSignatureValidator,
                assertionDecrypter,
                samlAssertionsSignatureValidator,
                responseFromIdpValidator,
                responseDestinationValidator,
                responseAssertionsFromIdpValidator);
    }

    @Test
    public void testThatSamlResponseSignatureValidatorUsesSPSSODescriptorRoleForValidation() {
        Response response = mock(Response.class);
        responseValidator.validate(response);
        verify(samlResponseSignatureValidator).validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }
}