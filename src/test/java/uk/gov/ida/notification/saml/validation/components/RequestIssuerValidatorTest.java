package uk.gov.ida.notification.saml.validation.components;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;

public class RequestIssuerValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static RequestIssuerValidator requestIssuerValidator;
    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;

    @BeforeClass
    public static void classSetup() throws Throwable {
        InitializationService.initialize();
        requestIssuerValidator = new RequestIssuerValidator();
    }

    @Before
    public void setUp() throws Throwable {
        eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder();
    }

    @Test
    public void shouldThrowExceptionIfNoIssuer() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Issuer");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutIssuer().build();
        requestIssuerValidator.validate(request.getIssuer());
    }

    @Test
    public void shouldThrowExceptionIfEmptyIssuer() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Issuer");

        AuthnRequest request = eidasAuthnRequestBuilder.withIssuer("").build();
        requestIssuerValidator.validate(request.getIssuer());
    }
}