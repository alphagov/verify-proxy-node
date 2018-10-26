package uk.gov.ida.notification.saml.validation.components;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;

public class LoaValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static LoaValidator loaValidator;
    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;


    @BeforeClass
    public static void classSetup() throws Throwable {
        InitializationService.initialize();
        loaValidator = new LoaValidator();
    }

    @Before
    public void setUp() throws Throwable {
        eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder();
    }

    @Test
    public void shouldNotThrowExceptionIfValidLoa() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        loaValidator.validate(request.getRequestedAuthnContext());
    }

    @Test
    public void shouldThrowExceptionIfNoRequestedAuthnContext() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing RequestedAuthnContext");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestedAuthnContext().build();
        loaValidator.validate(request.getRequestedAuthnContext());
    }

    @Test
    public void shouldThrowExceptionIfNoLoA() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing LoA");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutLoa().build();
        loaValidator.validate(request.getRequestedAuthnContext());
    }

    @Test
    public void shouldThrowExceptionIfEmptyLoA() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing LoA");

        AuthnRequest request = eidasAuthnRequestBuilder.withLoa("").build();
        loaValidator.validate(request.getRequestedAuthnContext());
    }

    @Test
    public void shouldThrowExceptionIfNonSubstantialLoA() throws Throwable {
        // we only handle substantial LoAs for now
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Invalid LoA 'http://eidas.europa.eu/LoA/high'");

        AuthnRequest request = eidasAuthnRequestBuilder.withLoa(EidasConstants.EIDAS_LOA_HIGH).build();
        loaValidator.validate(request.getRequestedAuthnContext());
    }

    @Test
    public void shouldThrowExceptionIfInvalidLoA() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Invalid LoA 'invalid'");

        AuthnRequest request = eidasAuthnRequestBuilder.withLoa("invalid").build();
        loaValidator.validate(request.getRequestedAuthnContext());
    }
}