package uk.gov.ida.notification.saml.validation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;

public class EidasAuthnRequestValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private EidasAuthnRequestValidator eidasAuthnRequestValidator;
    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;

    @Before
    public void setUp() throws Throwable {
        InitializationService.initialize();
        eidasAuthnRequestValidator = new EidasAuthnRequestValidator();
        eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder();
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
    public void shouldThrowExceptionIfNoIssuer() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Issuer");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutIssuer().build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfEmptyIssuer() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing Issuer");

        AuthnRequest request = eidasAuthnRequestBuilder.withIssuer("").build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldNOTThrowExceptionIfNoSpType() throws Throwable {
        // since SPType can be specified in metadata, requests with no SPType is valid
        AuthnRequest request = eidasAuthnRequestBuilder.withoutSpType().build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfNonPublicSpType() throws Throwable {
        // we only handle Public SPTypes for now
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Invalid SPType 'private'");

        AuthnRequest request = eidasAuthnRequestBuilder.withSpType(SPTypeEnumeration.PRIVATE.toString()).build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfInvalidSpType() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Invalid SPType 'invalid'");

        AuthnRequest request = eidasAuthnRequestBuilder.withSpType("invalid").build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfNoRequestedAuthnContext() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing RequestedAuthnContext");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestedAuthnContext().build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfNoLoA() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing LoA");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutLoa().build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfEmptyLoA() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing LoA");

        AuthnRequest request = eidasAuthnRequestBuilder.withLoa("").build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfNonSubstantialLoA() throws Throwable {
        // we only handle substantial LoAs for now
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Invalid LoA 'http://eidas.europa.eu/LoA/high'");

        AuthnRequest request = eidasAuthnRequestBuilder.withLoa(EidasConstants.EIDAS_LOA_HIGH).build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfInvalidLoA() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Invalid LoA 'invalid'");

        AuthnRequest request = eidasAuthnRequestBuilder.withLoa("invalid").build();
        eidasAuthnRequestValidator.validate(request);
    }

    @Test
    public void shouldThrowExceptionIfMissingNameIdPolicy() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing NameIdPolicy");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutNameIdPolicy().build();
        eidasAuthnRequestValidator.validate(request);
    }
}