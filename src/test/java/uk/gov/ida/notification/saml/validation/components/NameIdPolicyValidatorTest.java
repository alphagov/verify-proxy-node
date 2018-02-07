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

public class NameIdPolicyValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static NameIdPolicyValidator nameIdPolicyValidator;
    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;

    @BeforeClass
    public static void classSetup() throws Throwable {
        InitializationService.initialize();
        nameIdPolicyValidator = new NameIdPolicyValidator();
    }

    @Before
    public void setUp() throws Throwable {
        eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder();
    }
    @Test
    public void shouldThrowExceptionIfMissingNameIdPolicy() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing NameIdPolicy");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutNameIdPolicy().build();
        nameIdPolicyValidator.validate(request.getNameIDPolicy());
    }
}