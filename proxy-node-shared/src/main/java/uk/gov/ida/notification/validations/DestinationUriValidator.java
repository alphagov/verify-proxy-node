package uk.gov.ida.notification.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;

public class DestinationUriValidator implements ConstraintValidator<ValidDestinationUri, URI> {

    @Override
    public void initialize(ValidDestinationUri constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(URI potentialUri, ConstraintValidatorContext context) {
        // Detecting nulls, empties and whitespace is the responsibility of other validations.
        // Responding true here indicates that this validator does not have an opinion about empty values.
        if (potentialUri == null) {
            return true;
        }

        if (potentialUri.getScheme().equalsIgnoreCase("http") ||
            potentialUri.getScheme().equalsIgnoreCase("https")) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Destination url should use either http or https protocol.").addConstraintViolation();
        return false;
    }
}
