package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;

public class DestinationUriStringValidator implements ConstraintValidator<ValidDestinationUriString, String> {

    @Override
    public void initialize(ValidDestinationUriString constraint) { /* intentionally blank */ }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) { return true; } // @NotNull should detect nulls

        try {
            URI uri = URI.create(value);
            if (uri.getScheme().equalsIgnoreCase("http") ||
                uri.getScheme().equalsIgnoreCase("https")) {
                return true;
            } else {
                context.buildConstraintViolationWithTemplate("Destination url should use either http or https protocol.").addConstraintViolation();
                return false;
            }

        } catch (Exception e) {
            context.buildConstraintViolationWithTemplate("This is not a URI.").addConstraintViolation();
            return false; // could not create a URI from the String
        }
    }
}
