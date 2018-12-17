package uk.gov.ida.notification.saml.deprecate;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;

import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.authnContextClassRefMissing;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.authnContextClassRefValueMissing;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.authnContextMissingError;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.authnInstantMissing;

public class AuthnStatementAssertionValidator {
    private final DuplicateAssertionValidator duplicateAssertionValidator;

    public AuthnStatementAssertionValidator(DuplicateAssertionValidator duplicateAssertionValidator) {
        this.duplicateAssertionValidator = duplicateAssertionValidator;
    }

    public void validate(Assertion assertion) {
        validateAuthnStatement(assertion.getAuthnStatements().get(0));
        duplicateAssertionValidator.validateAuthnStatementAssertion(assertion);
    }

    private void validateAuthnStatement(AuthnStatement authnStatement) {
        if (authnStatement.getAuthnContext() == null)
            throw new SamlValidationException(authnContextMissingError());
        if (authnStatement.getAuthnContext().getAuthnContextClassRef() == null)
            throw new SamlValidationException(authnContextClassRefMissing());
        if (authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef() == null)
            throw new SamlValidationException(authnContextClassRefValueMissing());
        if (authnStatement.getAuthnInstant() == null)
            throw new SamlValidationException(authnInstantMissing());
    }
}
