package uk.gov.ida.notification.validations;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;

public class DestinationUriStringValidator implements ConstraintValidator<ValidDestinationUriString, String> {

    @Override
    public void initialize(ValidDestinationUriString constraint) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) { return true; } // @NotNull should detect nulls

        try {
            URI uri = URI.create(value);
            return
                uri.getScheme().toLowerCase().equals("http") ||
                uri.getScheme().toLowerCase().equals("https");

        } catch (Exception e) {
            return false; // could not create a URI from the String
        }
    }
}
