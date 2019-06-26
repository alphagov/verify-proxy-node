package uk.gov.ida.notification.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;

public class DestinationUriValidator implements ConstraintValidator<ValidDestinationUri, URI> {

    @Override
    public void initialize(ValidDestinationUri constraint) {
    }

    @Override
    public boolean isValid(URI value, ConstraintValidatorContext context) {
        if (value == null) { return true; } // @NotNull should detect nulls

        return
            value.getScheme().toLowerCase().equals("http") ||
            value.getScheme().toLowerCase().equals("https");
    }
}
