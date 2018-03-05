package uk.gov.ida.notification.saml.validation;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;
import uk.gov.ida.notification.saml.validation.components.ResponseAttributesValidator;
import uk.gov.ida.saml.hub.validators.response.idp.IdpResponseValidator;

public class HubResponseValidator {

    private final IdpResponseValidator idpResponseValidator;
    private final ResponseAttributesValidator responseAttributesValidator;

    public HubResponseValidator(IdpResponseValidator idpResponseValidator, ResponseAttributesValidator responseAttributesValidator) {
        this.idpResponseValidator = idpResponseValidator;
        this.responseAttributesValidator = responseAttributesValidator;
    }

    public void validate(Response response) {
        idpResponseValidator.validate(response);
        Assertion assertion = response.getAssertions().stream()
            .filter(a -> a.getAuthnStatements().isEmpty() && !a.getAttributeStatements().isEmpty())
            .findFirst()
            .orElseThrow(() -> new InvalidHubResponseException("Missing Matching Dataset Assertions"));
        AttributeStatement attributeStatement = assertion.getAttributeStatements().get(0);
        responseAttributesValidator.validate(attributeStatement);
    }
}
