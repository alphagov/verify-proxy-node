package uk.gov.ida.notification.stubconnector.validations;

import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.validations.ValidBase64Xml;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Base64SamlResponseValidator implements ConstraintValidator<ValidBase64SamlResponse, Response> {

    @Override
    public void initialize(ValidBase64SamlResponse constraintAnnotation) { /* intentionally blank */ }

    @Override
    public boolean isValid(Response response, ConstraintValidatorContext context) {
        return true; // if we got this far, it's valid
    }
}
