package uk.gov.ida.notification.saml.validation.components;

import org.opensaml.saml.saml2.core.Assertion;

import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import se.litsec.opensaml.saml2.common.response.MessageReplayException;
import uk.gov.ida.saml.core.validators.assertion.DuplicateAssertionValidator;
import uk.gov.ida.saml.hub.exception.SamlValidationException;

import static uk.gov.ida.saml.core.errors.SamlTransformationErrorFactory.authnStatementAlreadyReceived;
import static uk.gov.ida.saml.core.errors.SamlTransformationErrorFactory.duplicateMatchingDataset;

public class DuplicateAssertionChecker implements DuplicateAssertionValidator {
    private final MessageReplayChecker messageReplayChecker;

    public DuplicateAssertionChecker(final MessageReplayChecker messageReplayChecker) {
        this.messageReplayChecker = messageReplayChecker;
    }

    @Override
    public void validateAuthnStatementAssertion(Assertion assertion) {
        try {
            messageReplayChecker.checkReplay(assertion);
        } catch (MessageReplayException e) {
            throw new SamlValidationException(authnStatementAlreadyReceived(assertion.getID()));
        }
    }

    @Override
    public void validateMatchingDataSetAssertion(Assertion assertion, String responseIssuerId) {
        try {
            messageReplayChecker.checkReplay(assertion);
        } catch (MessageReplayException e) {
            throw new SamlValidationException(duplicateMatchingDataset(assertion.getID(), responseIssuerId));
        }
    }
}
