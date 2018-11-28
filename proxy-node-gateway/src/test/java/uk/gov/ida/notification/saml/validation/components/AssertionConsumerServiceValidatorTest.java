package uk.gov.ida.notification.saml.validation.components;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Issuer;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.helpers.TestMetadataBuilder;
import uk.gov.ida.saml.core.test.builders.AuthnRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.ida.saml.core.test.builders.AuthnRequestBuilder.anAuthnRequest;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;

public class AssertionConsumerServiceValidatorTest {
    private AssertionConsumerServiceValidator validator;
    private TestMetadataBuilder metadataBuilder;

    @Before
    public void setup() throws Exception {
        metadataBuilder = new TestMetadataBuilder("hub_metadata.xml");
        validator = new AssertionConsumerServiceValidator(metadataBuilder.buildResolver(AssertionConsumerServiceValidatorTest.class.getName()));
    }

    private AuthnRequestBuilder anAuthnRequestWithAssertionConsumerServiceUrl(String url) {
        return anAuthnRequest()
                .withIssuer(anIssuer()
                        .withIssuerId("https://dev-hub.local")
                        .build())
                .withAssertionConsumerServiceUrl(url);
    }

    @Test
    public void shouldNotThrowIfAssertionConsumerServiceIsMissing() {
        validator.validate(anAuthnRequestWithAssertionConsumerServiceUrl(null).build());
    }

    @Test
    public void shouldNotThrowIfAssertionConsumerServiceMatchesMetadata() {
        validator.validate(anAuthnRequestWithAssertionConsumerServiceUrl("http://localhost:6600/SAML2/SSO/Response/POST").build());
    }

    @Test
    public void shouldThrowIfAssertionConsumerServiceDoesNotMatchMetadata() {
        assertThrows(InvalidAuthnRequestException.class, () ->
                validator.validate(anAuthnRequestWithAssertionConsumerServiceUrl("https://eidas-service.eu/SAML2/SSO/Response/POST").build()));
    }

    @Test
    public void shouldThrowIfRequestIssuerNotFoundInMetadata() {
        Issuer unknownIssuer = anIssuer().withIssuerId("https://bogus.eu/POST").build();
        assertThrows(InvalidAuthnRequestException.class, () ->
                validator.validate(anAuthnRequestWithAssertionConsumerServiceUrl("http://localhost:6600/SAML2/SSO/Response/POST").withIssuer(unknownIssuer).build()));
    }
}
