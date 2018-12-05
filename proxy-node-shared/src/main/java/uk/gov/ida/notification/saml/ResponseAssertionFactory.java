package uk.gov.ida.notification.saml;

import org.opensaml.storage.ReplayCache;
import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import uk.gov.ida.notification.saml.validation.components.DuplicateAssertionChecker;
import uk.gov.ida.saml.core.validators.assertion.AssertionAttributeStatementValidator;
import uk.gov.ida.saml.core.validators.assertion.AuthnStatementAssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.DuplicateAssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.IPAddressValidator;
import uk.gov.ida.saml.core.validators.assertion.IdentityProviderAssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.MatchingDatasetAssertionValidator;
import uk.gov.ida.saml.core.validators.subject.AssertionSubjectValidator;
import uk.gov.ida.saml.core.validators.subjectconfirmation.AssertionSubjectConfirmationValidator;
import uk.gov.ida.saml.hub.validators.response.idp.components.ResponseAssertionsFromIdpValidator;
import uk.gov.ida.saml.security.validators.issuer.IssuerValidator;

import static uk.gov.ida.notification.saml.validation.components.MessageReplayCheckerFactory.createMessageReplayChecker;

public class ResponseAssertionFactory {

    public static ResponseAssertionsFromIdpValidator createResponseAssertionsFromIdpValidator(String application, String proxyNodeEntityId, ReplayCache cache) throws Exception {
        IdentityProviderAssertionValidator assertionValidator = new IdentityProviderAssertionValidator(
                new IssuerValidator(),
                new AssertionSubjectValidator(),
                new AssertionAttributeStatementValidator(),
                new AssertionSubjectConfirmationValidator()
        );
        MessageReplayChecker messageReplayChecker = createMessageReplayChecker(application + ":" + ResponseAssertionsFromIdpValidator.class.getName(), cache);
        DuplicateAssertionValidator duplicateAssertionValidator = new DuplicateAssertionChecker(messageReplayChecker);
        return new ResponseAssertionsFromIdpValidator(
                assertionValidator,
                new MatchingDatasetAssertionValidator(duplicateAssertionValidator),
                new AuthnStatementAssertionValidator(duplicateAssertionValidator),
                new IPAddressValidator(),
                proxyNodeEntityId
        );
    }
}
