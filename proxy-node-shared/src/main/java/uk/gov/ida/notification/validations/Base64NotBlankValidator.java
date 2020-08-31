package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;
import uk.gov.ida.Base64;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Base64NotBlankValidator implements ConstraintValidator<ValidBase64Xml, String> {

    @Override
    public void initialize(ValidBase64Xml constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String potentialBase64, ConstraintValidatorContext context) {
        // Detecting nulls, empties and whitespace is the responsibility of other validations.
        // Responding true here indicates that this validator does not have an opinion about empty values.
        if (StringUtils.isBlank(potentialBase64)) { return true; }

        try {
            String decoded = Base64.decodeToString(potentialBase64);
            return StringUtils.isNotBlank(decoded);

        } catch (RuntimeException e) {
            context.buildConstraintViolationWithTemplate("Exception: " + e.getMessage()).addConstraintViolation();
            return false; // could not create a URI from the String
        }
    }
}
