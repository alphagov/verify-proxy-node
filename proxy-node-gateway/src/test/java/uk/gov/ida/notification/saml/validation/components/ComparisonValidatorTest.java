package uk.gov.ida.notification.saml.validation.components;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextBuilder;

import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

public class ComparisonValidatorTest {
    private ComparisonValidator validator = new ComparisonValidator();

    private static RequestedAuthnContext aRequestedAuthnContext(AuthnContextComparisonTypeEnumeration comparison) {
        RequestedAuthnContext requestedAuthnContext = new RequestedAuthnContextBuilder().buildObject();
        requestedAuthnContext.setComparison(comparison);
        return requestedAuthnContext;
    }

    @Test
    public void shouldThrowIfAuthnContextIsMissing() {
        assertThrows(InvalidAuthnRequestException.class, () -> validator.validate(null));
    }

    @Test
    public void shouldNotThrowIfComparisonAttributeIsMissing() {
        validator.validate(aRequestedAuthnContext(null));
    }

    @Test
    public void shouldNotThrowIfComparisonAttributeIsMinimum() {
        validator.validate(aRequestedAuthnContext(AuthnContextComparisonTypeEnumeration.MINIMUM));
    }

    @Test
    public void shouldThrowIfComparisonAttributeIsNotMinimum() {
        assertThrows(InvalidAuthnRequestException.class, () -> validator.validate(aRequestedAuthnContext(AuthnContextComparisonTypeEnumeration.MAXIMUM)));
        assertThrows(InvalidAuthnRequestException.class, () -> validator.validate(aRequestedAuthnContext(AuthnContextComparisonTypeEnumeration.EXACT)));
        assertThrows(InvalidAuthnRequestException.class, () -> validator.validate(aRequestedAuthnContext(AuthnContextComparisonTypeEnumeration.BETTER)));
    }
}