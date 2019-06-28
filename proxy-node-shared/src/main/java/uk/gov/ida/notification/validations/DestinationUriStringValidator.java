package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;

public class DestinationUriStringValidator implements ConstraintValidator<ValidDestinationUriString, String> {

    @Override
    public void initialize(ValidDestinationUriString constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String potentialUri, ConstraintValidatorContext context) {
        // Detecting nulls, empties and whitespace is the responsibility of other validations.
        // Responding true here indicates that this validator does not have an opinion about empty values.
        if (StringUtils.isBlank(potentialUri)) { return true; }

        try {
            URI uri = URI.create(potentialUri);
            if (uri.getScheme().equalsIgnoreCase("http") ||
                uri.getScheme().equalsIgnoreCase("https")) {
                return true;
            } else {
                context.buildConstraintViolationWithTemplate("Destination url should use either http or https protocol.").addConstraintViolation();
                return false;
            }

        } catch (Exception e) {
            // none of the code above throws explicit exceptions, however if there's a problem that means the String
            // can't be resolved into a URI object, we should catch it and fail it in this validator.
            context.buildConstraintViolationWithTemplate("Exception: " + e.getMessage()).addConstraintViolation();
            return false; // could not create a URI from the String
        }
    }
}
