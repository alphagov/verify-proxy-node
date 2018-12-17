package uk.gov.ida.notification.saml;

import se.litsec.opensaml.saml2.common.response.MessageReplayChecker;
import uk.gov.ida.notification.saml.deprecate.AssertionAttributeStatementValidator;
import uk.gov.ida.notification.saml.deprecate.AssertionSubjectConfirmationValidator;
import uk.gov.ida.notification.saml.deprecate.AssertionSubjectValidator;
import uk.gov.ida.notification.saml.deprecate.AuthnStatementAssertionValidator;
import uk.gov.ida.notification.saml.deprecate.DuplicateAssertionValidator;
import uk.gov.ida.notification.saml.deprecate.IPAddressValidator;
import uk.gov.ida.notification.saml.deprecate.IdentityProviderAssertionValidator;
import uk.gov.ida.notification.saml.deprecate.MatchingDatasetAssertionValidator;
import uk.gov.ida.notification.saml.deprecate.ResponseAssertionsFromIdpValidator;
import uk.gov.ida.notification.saml.validation.components.DuplicateAssertionChecker;
import uk.gov.ida.saml.security.validators.issuer.IssuerValidator;

public class ResponseAssertionFactory {

    public static ResponseAssertionsFromIdpValidator createResponseAssertionsFromIdpValidator(String application, String proxyNodeEntityId, MessageReplayChecker messageReplayChecker) throws Exception {
        IdentityProviderAssertionValidator assertionValidator = new IdentityProviderAssertionValidator(
                new IssuerValidator(),
                new AssertionSubjectValidator(),
                new AssertionAttributeStatementValidator(),
                new AssertionSubjectConfirmationValidator()
        );
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
