package uk.gov.ida.notification.saml.deprecate;

import org.opensaml.saml.saml2.core.Assertion;

public interface DuplicateAssertionValidator {
    void validateAuthnStatementAssertion(Assertion assertion);

    void validateMatchingDataSetAssertion(Assertion assertion, String responseIssuerId);
}
