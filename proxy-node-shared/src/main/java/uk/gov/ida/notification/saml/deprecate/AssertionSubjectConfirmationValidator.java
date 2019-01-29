package uk.gov.ida.notification.saml.deprecate;

import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.core.validation.SamlValidationSpecificationFailure;

public class AssertionSubjectConfirmationValidator extends BasicAssertionSubjectConfirmationValidator {

    public void validate(
            SubjectConfirmation subjectConfirmation,
            String requestId) {

        super.validate(subjectConfirmation);

        SubjectConfirmationData subjectConfirmationData = subjectConfirmation.getSubjectConfirmationData();

        if (!subjectConfirmationData.getInResponseTo().equals(requestId)) {
            SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.notMatchInResponseTo(subjectConfirmationData.getInResponseTo(), requestId);
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }
 }
