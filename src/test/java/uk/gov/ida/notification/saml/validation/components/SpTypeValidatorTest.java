package uk.gov.ida.notification.saml.validation.components;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import se.litsec.eidas.opensaml.ext.impl.SPTypeImpl;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;

import java.util.Optional;

public class SpTypeValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static SpTypeValidator spTypeValidator;
    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;

    @BeforeClass
    public static void classSetup() throws Throwable {
        InitializationService.initialize();
        spTypeValidator = new SpTypeValidator();
    }

    @Before
    public void setUp() throws Throwable {
        eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder();
    }

    @Test
    public void shouldNotThrowExceptionIfValidSpType() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        spTypeValidator.validate(getSpType(request));
    }

    @Test
    public void shouldNOTThrowExceptionIfNoSpType() throws Throwable {
        // since SPType can be specified in metadata, requests with no SPType is valid
        AuthnRequest request = eidasAuthnRequestBuilder.withoutSpType().build();
        spTypeValidator.validate(getSpType(request));
    }

    @Test
    public void shouldThrowExceptionIfNonPublicSpType() throws Throwable {
        // we only handle Public SPTypes for now
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Invalid SPType 'private'");

        AuthnRequest request = eidasAuthnRequestBuilder.withSpType(SPTypeEnumeration.PRIVATE.toString()).build();
        spTypeValidator.validate(getSpType(request));
    }

    @Test
    public void shouldThrowExceptionIfInvalidSpType() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Invalid SPType 'invalid'");

        AuthnRequest request = eidasAuthnRequestBuilder.withSpType("invalid").build();
        spTypeValidator.validate(getSpType(request));
    }

    private Optional<XMLObject> getSpType(AuthnRequest request) {
        return request.getExtensions().getOrderedChildren()
                .stream()
                .filter(obj -> obj instanceof SPTypeImpl)
                .findFirst();
    }

}