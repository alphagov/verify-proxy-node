package uk.gov.ida.notification.saml.validation;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;
import uk.gov.ida.notification.saml.validation.components.LoaValidator;
import uk.gov.ida.notification.saml.validation.components.ResponseAttributesValidator;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.hub.validators.response.idp.IdpResponseValidator;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.saml.security.validators.ValidatedResponse;

public class HubResponseValidator {

    private final IdpResponseValidator idpResponseValidator;
    private final ResponseAttributesValidator responseAttributesValidator;
    private final LoaValidator loaValidator;

    public HubResponseValidator(
        IdpResponseValidator idpResponseValidator,
        ResponseAttributesValidator responseAttributesValidator,
        LoaValidator loaValidator) {
        this.idpResponseValidator = idpResponseValidator;
        this.responseAttributesValidator = responseAttributesValidator;
        this.loaValidator = loaValidator;
    }

    public ValidatedResponse getValidatedResponse() {
        return idpResponseValidator.getValidatedResponse();
    }

    public ValidatedAssertions getValidatedAssertions() {
        return idpResponseValidator.getValidatedAssertions();
    }

    public void validate(Response response) {
        try {
            idpResponseValidator.validate(response);

            Assertion matchingDatasetAssertion = getValidatedAssertions()
                .getMatchingDatasetAssertion()
                .orElseThrow(() -> new InvalidHubResponseException("Missing Matching Dataset Assertions"));
            responseAttributesValidator.validate(matchingDatasetAssertion.getAttributeStatements().get(0));

            Assertion authnContextAssertion = getValidatedAssertions()
                .getAuthnStatementAssertion()
                .orElseThrow(() -> new InvalidHubResponseException("Missing Authn Statement Assertion"));
            loaValidator.validate(authnContextAssertion.getAuthnStatements().get(0).getAuthnContext());
        } catch (SamlTransformationErrorException exception) {
            throw new InvalidHubResponseException(exception.getMessage(), exception);
        }
    }
}
