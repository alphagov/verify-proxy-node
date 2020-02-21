package uk.gov.ida.notification.eidassaml.saml.validation.components;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.contracts.metadata.AssertionConsumerService;
import uk.gov.ida.notification.contracts.metadata.CountryMetadataResponse;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.shared.proxy.MetatronProxy;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssertionConsumerServiceValidatorTest {

    @Mock
    private static MetatronProxy metatronProxy;

    private AssertionConsumerServiceValidator assertionConsumerServiceValidator;

    @BeforeClass
    public static void setUp() throws Exception {
        InitializationService.initialize();
    }

    @Before
    public void before() {
        assertionConsumerServiceValidator = new AssertionConsumerServiceValidator(metatronProxy);
    }

    @Test
    public void shouldNotThrowIfAssertionConsumerServiceUrlNotProvidedInTheAuthenticationRequest() throws Exception {
        final AuthnRequest eidasAuthnRequest = new EidasAuthnRequestBuilder().build();

        assertionConsumerServiceValidator.validate(eidasAuthnRequest);

        assertThat(eidasAuthnRequest.getAssertionConsumerServiceURL()).isNull();
    }

    @Test
    public void shouldNotThrowIfAssertionConsumerServiceUrlProvidedInTheAuthenticationRequestHasAMatchInTheMetadata() throws Exception {
        final AuthnRequest eidasAuthnRequest = new EidasAuthnRequestBuilder()
                .withIssuer("issuer")
                .withAssertionConsumerServiceURL("http://www.eidas.com/Response/POST")
                .build();

        final CountryMetadataResponse countryMetadataResponse = new CountryMetadataResponse(
                null,
                null,
                Collections.singletonList(new AssertionConsumerService(URI.create("http://www.eidas.com/Response/POST"), 0, true)),
                null,
                null
        );

        when(metatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue())).thenReturn(countryMetadataResponse);

        assertionConsumerServiceValidator.validate(eidasAuthnRequest);

        assertThat(eidasAuthnRequest.getAssertionConsumerServiceURL()).isEqualTo("http://www.eidas.com/Response/POST");
        assertThat(metatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue()).getAssertionConsumerServices().get(0).getLocation().toString()).isEqualTo("http://www.eidas.com/Response/POST");
    }

    @Test
    public void shouldThrowIfAssertionConsumerServiceUrlProvidedInTheAuthenticationRequestHasNoMatchInTheMetadata() throws Exception {
        final AuthnRequest eidasAuthnRequest = new EidasAuthnRequestBuilder()
                .withIssuer("issuer")
                .withAssertionConsumerServiceURL("http://www.invalid.com/Response/POST")
                .build();

        final CountryMetadataResponse countryMetadataResponse = new CountryMetadataResponse(
                null,
                null,
                Collections.singletonList(new AssertionConsumerService(URI.create("http://www.eidas.com/Response/POST"), 0, true)),
                null,
                null
        );

        when(metatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue())).thenReturn(countryMetadataResponse);

        assertThatThrownBy(() -> assertionConsumerServiceValidator.validate(eidasAuthnRequest)).isInstanceOf(InvalidAuthnRequestException.class);

        assertThat(eidasAuthnRequest.getAssertionConsumerServiceURL()).isEqualTo("http://www.invalid.com/Response/POST");
        assertThat(metatronProxy.getCountryMetadata(eidasAuthnRequest.getIssuer().getValue()).getAssertionConsumerServices().get(0).getLocation().toString()).isEqualTo("http://www.eidas.com/Response/POST");
    }
}
