package uk.gov.ida.notification.saml.deprecate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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

    @Mock
    private static AssertionDecrypter assertionDecrypter;
    @Mock
    private static DestinationValidator responseDestinationValidator;
    @Mock
    private static EncryptedResponseFromIdpValidator responseFromIdpValidator;
    @Mock
    private static SamlResponseSignatureValidator samlResponseSignatureValidator;
    @Mock
    private static SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;
    @Mock
    private static ResponseAssertionsFromIdpValidator responseAssertionsFromIdpValidator;

    @InjectMocks
    private static IdpResponseValidator responseValidator;

    @Test
    public void testThatSamlResponseSignatureValidatorUsesSPSSODescriptorRoleForValidation() {
        Response response = mock(Response.class);
        responseValidator.validate(response);
        verify(samlResponseSignatureValidator).validate(response, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }
}
