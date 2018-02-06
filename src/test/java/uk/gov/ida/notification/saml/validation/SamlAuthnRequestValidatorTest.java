package uk.gov.ida.notification.saml.validation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;

public class SamlAuthnRequestValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private SamlAuthnRequestValidator samlMessageValidator;
    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;

    @Before
    public void setUp() throws Throwable {
        InitializationService.initialize();
        samlMessageValidator = new SamlAuthnRequestValidator();
        eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder();
    }

    @Test
    public void shouldNotThrowExceptionIfValidAuthnRequest() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        samlMessageValidator.validateAuthnRequest(request);
    }

    @Test
    public void shouldThrowExceptionIfNullAuthnRequest() {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Null request");

        samlMessageValidator.validateAuthnRequest(null);
    }
    
    @Test
    public void shouldThrowExceptionIfInvalidIssuer() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Issuer");

        AuthnRequest request = eidasAuthnRequestBuilder.withNoIssuer().build();
        samlMessageValidator.validateAuthnRequest(request);
    }
}