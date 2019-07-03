package uk.gov.ida.notification.saml.deprecate;

import com.google.common.base.Strings;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.validation.SamlValidationSpecificationFailure;

import java.util.List;

import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.invalidStatusCode;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.invalidSubStatusCode;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.missingId;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.missingIssueInstant;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.missingSignature;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.missingSuccessUnEncryptedAssertions;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.nestedSubStatusCodesBreached;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.nonSuccessHasUnEncryptedAssertions;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.signatureNotSigned;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.unencryptedAssertion;
import static uk.gov.ida.notification.saml.deprecate.SamlTransformationErrorFactory.unexpectedNumberOfAssertions;
import static uk.gov.ida.saml.security.validators.signature.SamlSignatureUtil.isSignaturePresent;

public class EncryptedResponseFromIdpValidator<T extends Enum> {
    private static final int SUB_STATUS_CODE_LIMIT = 1;
    private SamlStatusToAuthenticationStatusCodeMapper<T> statusCodeMapper;

    public EncryptedResponseFromIdpValidator(final SamlStatusToAuthenticationStatusCodeMapper<T> statusCodeMapper) {
        this.statusCodeMapper = statusCodeMapper;
    }

    protected void validateAssertionPresence(Response response) {
        if (!response.getAssertions().isEmpty()) throw new SamlValidationException(unencryptedAssertion());

        boolean responseWasSuccessful = response.getStatus().getStatusCode().getValue().equals(StatusCode.SUCCESS);
        List<EncryptedAssertion> encryptedAssertions = response.getEncryptedAssertions();

        if (responseWasSuccessful && encryptedAssertions.isEmpty()) {
            throw new SamlValidationException(missingSuccessUnEncryptedAssertions());
        }

        if (!responseWasSuccessful && !encryptedAssertions.isEmpty()) {
            throw new SamlValidationException(nonSuccessHasUnEncryptedAssertions());
        }

        if (responseWasSuccessful && encryptedAssertions.size() != 2) {
            throw new SamlValidationException(unexpectedNumberOfAssertions(2, encryptedAssertions.size()));
        }
    }

    public void validate(Response response) {
        validateResponse(response);
    }

    private void validateResponse(Response response) {
        if (Strings.isNullOrEmpty(response.getID())) throw new SamlValidationException(missingId());
        if (response.getIssueInstant() == null) throw new SamlValidationException(missingIssueInstant(response.getID()));

        Signature signature = response.getSignature();
        if (signature == null) throw new SamlValidationException(missingSignature());
        if (!isSignaturePresent(signature)) throw new SamlValidationException(signatureNotSigned());

        validateStatus(response.getStatus());
        validateAssertionPresence(response);
    }

    private void validateStatus(Status status) {
        validateStatusCode(status.getStatusCode(), 0);
    }

    private void fail(Status status) {
        StatusCode statusCode = status.getStatusCode();
        StatusCode subStatusCode = statusCode.getStatusCode();

        if (subStatusCode == null) throw new SamlValidationException(invalidStatusCode(statusCode.getValue()));

        SamlValidationSpecificationFailure failure = invalidSubStatusCode(
                subStatusCode.getValue(),
                statusCode.getValue()
        );
        throw new SamlValidationException(failure);
    }

    private void validateStatusCode(StatusCode statusCode, int subStatusCount) {
        if (subStatusCount > SUB_STATUS_CODE_LIMIT) {
            throw new SamlValidationException(nestedSubStatusCodesBreached(SUB_STATUS_CODE_LIMIT));
        }

        StatusCode subStatus = statusCode.getStatusCode();
        if (subStatus != null) validateStatusCode(subStatus, subStatusCount + 1);
    }
}
