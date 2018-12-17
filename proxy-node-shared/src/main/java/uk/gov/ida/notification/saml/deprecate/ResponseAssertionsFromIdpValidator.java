package uk.gov.ida.notification.saml.deprecate;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.saml.security.validators.ValidatedResponse;

import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.missingAuthnStatement;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.missingMatchingMds;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.multipleAuthnStatements;

public class ResponseAssertionsFromIdpValidator {

    private final IdentityProviderAssertionValidator identityProviderAssertionValidator;
    private final MatchingDatasetAssertionValidator matchingDatasetAssertionValidator;
    private final AuthnStatementAssertionValidator authnStatementAssertionValidator;
    private final IPAddressValidator ipAddressValidator;
    private String hubEntityId;

    public ResponseAssertionsFromIdpValidator(IdentityProviderAssertionValidator assertionValidator,
                                              MatchingDatasetAssertionValidator matchingDatasetAssertionValidator,
                                              AuthnStatementAssertionValidator authnStatementAssertionValidator,
                                              IPAddressValidator ipAddressValidator,
                                              String hubEntityId) {
        this.identityProviderAssertionValidator = assertionValidator;
        this.matchingDatasetAssertionValidator = matchingDatasetAssertionValidator;
        this.authnStatementAssertionValidator = authnStatementAssertionValidator;
        this.ipAddressValidator = ipAddressValidator;
        this.hubEntityId = hubEntityId;
    }

    public void validate(ValidatedResponse validatedResponse, ValidatedAssertions validatedAssertions) {
        validatedAssertions.getAssertions().forEach(
            assertion -> identityProviderAssertionValidator.validate(assertion, validatedResponse.getInResponseTo(), hubEntityId)
        );

        if (!validatedResponse.isSuccess()) return;

        Assertion matchingDatasetAssertion = getMatchingDatasetAssertion(validatedAssertions);
        Assertion authnStatementAssertion = getAuthnStatementAssertion(validatedAssertions);

        if (authnStatementAssertion.getAuthnStatements().size() > 1) {
            throw new SamlValidationException(multipleAuthnStatements());
        }

        matchingDatasetAssertionValidator.validate(matchingDatasetAssertion, validatedResponse.getIssuer().getValue());
        authnStatementAssertionValidator.validate(authnStatementAssertion);
        identityProviderAssertionValidator.validateConsistency(authnStatementAssertion, matchingDatasetAssertion);
        ipAddressValidator.validate(authnStatementAssertion);
    }

    private Assertion getAuthnStatementAssertion(ValidatedAssertions validatedAssertions) {
        return validatedAssertions.getAuthnStatementAssertion().orElseThrow(() -> new SamlValidationException(missingAuthnStatement()));
    }

    private Assertion getMatchingDatasetAssertion(ValidatedAssertions validatedAssertions) {
        return validatedAssertions.getMatchingDatasetAssertion().orElseThrow(() -> new SamlValidationException(missingMatchingMds()));
    }
}
