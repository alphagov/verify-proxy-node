package uk.gov.ida.notification.saml.validation.components;

import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;

import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

public class ComparisonValidator {
    public static final AuthnContextComparisonTypeEnumeration validType = AuthnContextComparisonTypeEnumeration.MINIMUM;

    public void validate(RequestedAuthnContext authnContext) {
        if (authnContext == null) {
            throw new InvalidAuthnRequestException("Request has no requested authentication context");
        }

        AuthnContextComparisonTypeEnumeration comparisonType = authnContext.getComparison();
        if (comparisonType != null && !validType.equals(comparisonType)) {
            throw new InvalidAuthnRequestException("Comparison type, if present, must be " + validType.toString());
        }
    }
}
