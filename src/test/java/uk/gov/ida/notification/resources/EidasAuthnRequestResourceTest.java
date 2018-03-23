package uk.gov.ida.notification.resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.EidasProxyNodeConfiguration;
import uk.gov.ida.notification.HubAuthnRequestGenerator;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.saml.validation.EidasAuthnRequestValidator;
import uk.gov.ida.saml.security.validators.signature.SamlRequestSignatureValidator;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class EidasAuthnRequestResourceTest {

    @Mock
    private EidasProxyNodeConfiguration configuration;
    @Mock
    private HubAuthnRequestGenerator authnRequestGenerator;
    @Mock
    private SamlFormViewBuilder samlFormViewBuilder;
    @Mock
    private EidasAuthnRequestValidator eidasAuthnRequestValidator;
    @Mock
    private SamlRequestSignatureValidator samlRequestSignatureValidator;

    private EidasAuthnRequestResource eidasAuthnRequestResource;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Before
    public void setUp() {
        eidasAuthnRequestResource = new EidasAuthnRequestResource(configuration, authnRequestGenerator, samlFormViewBuilder, eidasAuthnRequestValidator, samlRequestSignatureValidator);
    }

    @Test
    public void shouldFailWithSignatureValidationIfBothSignatureAndMessageBodyInvalid() {
        InvalidAuthnRequestException expectedCause = new InvalidAuthnRequestException("Invalid Signature!!");

        AuthnRequest encodedAuthnRequest = mock(AuthnRequest.class);
        doThrow(new InvalidAuthnRequestException("Invalid Body!!")).when(eidasAuthnRequestValidator).validate(any());
        doThrow(expectedCause).when(samlRequestSignatureValidator).validate(any(), any());

        expectedException.expectCause(is(expectedCause));
        eidasAuthnRequestResource.handlePostBinding(encodedAuthnRequest, "RelayState");
    }
}